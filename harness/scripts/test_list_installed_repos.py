import importlib.util
import json
import pathlib
import sys
import types
import unittest
from unittest.mock import patch


MODULE_PATH = pathlib.Path(__file__).with_name("list-installed-repos.py")
SPEC = importlib.util.spec_from_file_location("list_installed_repos", MODULE_PATH)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
# PyYAML이 없는 환경에서도 모듈을 import할 수 있게 하는 용도로만 쓴다.
# 설치된 PyYAML을 빈 모듈로 가리면 같은 프로세스의 다른 테스트가 깨지므로 import가 실패할 때만 넣는다.
try:
    import yaml  # noqa: F401
except ModuleNotFoundError:
    sys.modules.setdefault("yaml", types.ModuleType("yaml"))
SPEC.loader.exec_module(MODULE)


class _StubYAMLError(Exception):
    pass


# 실제 PyYAML 모듈을 건드리지 않도록 테스트 대상 모듈의 yaml만 private stub으로 재바인딩한다.
_yaml_stub = types.SimpleNamespace(safe_load=lambda source: source, YAMLError=_StubYAMLError)
setattr(MODULE, "yaml", _yaml_stub)


class GetRepoHarnessConfigTest(unittest.TestCase):
    def call_config(self, result):
        with patch.object(MODULE.subprocess, "run", return_value=result):
            return MODULE.get_repo_harness_config("acme/api", "test-token")

    def result(self, returncode=0, stdout="", stderr=""):
        return types.SimpleNamespace(returncode=returncode, stdout=stdout, stderr=stderr)

    def test_404_returns_none(self):
        self.assertIsNone(self.call_config(self.result(1, stderr="gh: Not Found (HTTP 404)")))

    def test_403_raises_config_fetch_error(self):
        with self.assertRaisesRegex(MODULE.ConfigFetchError, r"acme/api: gh api 실패\(exit 1\)"):
            self.call_config(self.result(1, stderr="gh: Forbidden (HTTP 403)"))

    def test_404_text_outside_status_line_is_not_treated_as_missing(self):
        stderr = "gh: Forbidden (HTTP 403)\nsee docs for (HTTP 404) handling"
        with self.assertRaises(MODULE.ConfigFetchError):
            self.call_config(self.result(1, stderr=stderr))

    def test_empty_stderr_reports_exit_code(self):
        with self.assertRaisesRegex(MODULE.ConfigFetchError, r"exit 4\): 알 수 없는 오류"):
            self.call_config(self.result(4))

    def test_yaml_error_raises_config_fetch_error(self):
        payload = {"content": "Z3JvdXBzOiBb"}
        with patch.object(MODULE.yaml, "safe_load", side_effect=_StubYAMLError("bad")):
            with self.assertRaisesRegex(MODULE.ConfigFetchError, "YAML 파싱 실패"):
                self.call_config(self.result(stdout=json.dumps(payload)))

    def test_valid_config_returns_dict(self):
        payload = {"content": "Z3JvdXBzOiBbY2xhdWRlXQ=="}
        with patch.object(MODULE.yaml, "safe_load", return_value={"groups": ["claude"]}):
            self.assertEqual(self.call_config(self.result(stdout=json.dumps(payload))), {"groups": ["claude"]})

    def test_empty_config_returns_empty_dict(self):
        payload = {"content": ""}
        with patch.object(MODULE.yaml, "safe_load", return_value=None):
            self.assertEqual(self.call_config(self.result(stdout=json.dumps(payload))), {})

    def test_non_dict_config_raises_error(self):
        payload = {"content": "W10="}
        with patch.object(MODULE.yaml, "safe_load", return_value=[]):
            with self.assertRaises(MODULE.ConfigFetchError):
                self.call_config(self.result(stdout=json.dumps(payload)))

    def test_bad_json_raises_error(self):
        with self.assertRaises(MODULE.ConfigFetchError):
            self.call_config(self.result(stdout="not json"))


class ResolveFilesTest(unittest.TestCase):
    manifest = {"items": [
        {"id": "a", "src": "a/", "dest": "a/", "groups": ["core"]},
        {"id": "b", "src": "b/", "dest": "b/", "groups": ["extra"]},
        {"id": "a@v2", "src": "archive/a/v2/", "dest": "a/", "groups": []},
        {"id": "claude/settings", "src": "settings", "dest": ".claude/settings.json", "groups": []},
        {"id": "claude/hooks/one", "src": "hook", "dest": ".claude/hooks/one", "groups": []},
    ]}

    def resolve(self, config, defaults=("core",)):
        return MODULE.resolve_files(config, self.manifest, list(defaults))

    def test_none_selects_default_groups(self):
        self.assertEqual(self.resolve(None), [("a/", "a/")])

    def test_groups_and_boolean_overrides(self):
        self.assertEqual(self.resolve({"groups": ["extra"], "overrides": {"a": True, "b": False}}), [("a/", "a/")])

    def test_version_pin_uses_version_source_and_canonical_destination(self):
        self.assertEqual(self.resolve({"groups": [], "overrides": {"a": "v2"}}), [("archive/a/v2/", "a/")])

    def test_legacy_exclude_include(self):
        self.assertEqual(self.resolve({"groups": ["core"], "exclude": ["a"], "include": ["b"]}), [("b/", "b/")])

    def test_settings_source_switches_with_hook_selection(self):
        without_hook = self.resolve({"groups": [], "overrides": {"claude/settings": True}})
        with_hook = self.resolve({"groups": [], "overrides": {"claude/settings": True, "claude/hooks/one": True}})
        self.assertEqual(without_hook[0][0], MODULE.SETTINGS_BASE)
        self.assertEqual(with_hook[0][0], MODULE.SETTINGS_WITH_HOOKS)


class BuildSyncConfigTest(unittest.TestCase):
    def test_prefixes_source_but_not_destination(self):
        config = MODULE.build_sync_config("acme/api", [(".claude/skills/a/", ".claude/skills/a/")], "develop", "harness/")
        self.assertIn("      acme/api@develop\n", config)
        self.assertIn("      - source: harness/.claude/skills/a/\n", config)
        self.assertIn("        dest: .claude/skills/a/\n", config)

    def test_keeps_source_unchanged_without_prefix(self):
        config = MODULE.build_sync_config("acme/api", [("a/", "a/")], "main")
        self.assertIn("      - source: a/\n", config)

    def test_empty_files_produce_empty_group(self):
        self.assertEqual(MODULE.build_sync_config("acme/api", [], "main", "harness/"), "group: []")


class GhApiTest(unittest.TestCase):
    def call_api(self, pages):
        with patch.object(MODULE.subprocess, "run") as run:
            run.return_value.stdout = json.dumps(pages)
            result = MODULE.gh_api("/installation/repositories", "test-token")
            self.assertEqual(
                run.call_args.args[0],
                ["gh", "api", "/installation/repositories", "--paginate", "--slurp"],
            )
        return result

    def test_merges_repository_objects_across_pages(self):
        self.assertEqual(
            self.call_api([
                {"total_count": 2, "repositories": [{"full_name": "acme/api"}]},
                {"total_count": 2, "repositories": [{"full_name": "acme/web"}]},
            ]),
            {
                "total_count": 2,
                "repositories": [{"full_name": "acme/api"}, {"full_name": "acme/web"}],
            },
        )

    def test_preserves_single_repository_page(self):
        page = {"total_count": 1, "repositories": [{"full_name": "acme/api"}]}
        self.assertEqual(self.call_api([page]), page)

    def test_preserves_empty_repository_list(self):
        page = {"total_count": 0, "repositories": []}
        self.assertEqual(self.call_api([page]), page)

    def test_merges_array_pages_without_corrupting_strings(self):
        self.assertEqual(
            self.call_api([[{"name": "a][b"}], [], [{"name": "web"}]]),
            [{"name": "a][b"}, {"name": "web"}],
        )

    def test_preserves_empty_array(self):
        self.assertEqual(self.call_api([[]]), [])

    def test_preserves_other_single_objects(self):
        self.assertEqual(self.call_api([{"id": 123}]), {"id": 123})


class SelectTargetRepositoriesTest(unittest.TestCase):
    def test_returns_only_the_exact_requested_repository(self):
        repositories = [
            {"full_name": "acme/api"},
            {"full_name": "acme/web"},
        ]

        self.assertEqual(
            MODULE.select_target_repositories(repositories, "acme/web"),
            [{"full_name": "acme/web"}],
        )


if __name__ == "__main__":
    unittest.main()
