import contextlib
import importlib.util
import io
import pathlib
import subprocess
import sys
import tempfile
import textwrap
import unittest


MODULE_PATH = pathlib.Path(__file__).with_name("validate-manifest.py")
SPEC = importlib.util.spec_from_file_location("validate_manifest", MODULE_PATH)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
sys.modules["validate_manifest"] = MODULE
SPEC.loader.exec_module(MODULE)


MANIFEST = """\
items:
  - id: claude/skills/demo
    src: .claude/skills/demo/
    dest: .claude/skills/demo/
    groups: [claude]

  - id: codex/skills/demo
    src: .agents/skills/demo/
    dest: .agents/skills/demo/
    groups: [codex]

  - id: claude/agents/helper
    src: .claude/agents/helper.md
    dest: .claude/agents/helper.md
    groups: [claude]

  - id: codex/agents/helper
    src: .codex/agents/helper.toml
    dest: .codex/agents/helper.toml
    groups: [codex]

  - id: claude/hooks/guard
    src: .claude/hooks/modules/guard/
    dest: .claude/hooks/modules/guard/
    groups: []

  - id: codex/hooks/guard
    src: .codex/hooks/modules/guard/
    dest: .codex/hooks/modules/guard/
    groups: []

defaults:
  - claude
  - codex

deletions:
  - dest: .claude/skills/old/
"""

SKILL = "---\nname: demo\ndescription: Demo skill.\n---\n\n# Demo\n"
CLAUDE_AGENT = "---\nname: helper\ndescription: Helper agent.\n---\n\nBody\n"
CODEX_AGENT = textwrap.dedent("""\
    name = "helper"
    description = "Helper agent."
    model_reasoning_effort = "low"
    sandbox_mode = "read-only"
    developer_instructions = '''
    grep -E '\\d+'
    '''
""")


class ValidateManifestTest(unittest.TestCase):
    def setUp(self):
        self._tmp = tempfile.TemporaryDirectory()
        self.root = pathlib.Path(self._tmp.name)
        self.write("sync-manifest.yml", MANIFEST)
        self.write(".claude/skills/demo/SKILL.md", SKILL)
        self.write(".agents/skills/demo/SKILL.md", SKILL)
        self.write(".claude/agents/helper.md", CLAUDE_AGENT)
        self.write(".claude/agents/README.md", "# Agents\n")
        self.write(".codex/agents/helper.toml", CODEX_AGENT)
        self.write(".claude/hooks/modules/guard/preToolUse.sh", "exit 0\n")
        self.write(".codex/hooks/modules/guard/pre-tool-use.sh", "exit 0\n")

    def tearDown(self):
        self._tmp.cleanup()

    def write(self, rel, content):
        path = self.root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8")

    def replace(self, rel, old, new):
        path = self.root / rel
        text = path.read_text(encoding="utf-8")
        self.assertIn(old, text)
        path.write_text(text.replace(old, new), encoding="utf-8")

    def run_validate(self, changed=None):
        return MODULE.validate(self.root, changed)

    def assert_error(self, report, fragment):
        messages = [f"{e.path}: {e.message}" for e in report.errors]
        self.assertTrue(any(fragment in m for m in messages), f"'{fragment}' 에러 없음: {messages}")

    def assert_warning(self, report, fragment):
        messages = [f"{w.path}: {w.message}" for w in report.warnings]
        self.assertTrue(any(fragment in m for m in messages), f"'{fragment}' 경고 없음: {messages}")

    def test_valid_fixture_has_no_findings(self):
        report = self.run_validate()
        self.assertEqual(report.errors, [])
        self.assertEqual(report.warnings, [])

    # ── manifest ──

    def test_missing_src_directory(self):
        self.replace("sync-manifest.yml", "src: .claude/skills/demo/", "src: .claude/skills/demo-typo/")
        report = self.run_validate()
        self.assert_error(report, "src 디렉터리가 없습니다 (.claude/skills/demo-typo/)")
        # 오타로 실제 디렉터리가 미등록 상태가 되는 것도 같이 잡는다
        self.assert_error(report, ".claude/skills/demo: claude skill 'demo'가 sync-manifest.yml에 등록되지 않았습니다")

    def test_error_reports_manifest_line(self):
        self.replace("sync-manifest.yml", "src: .agents/skills/demo/", "src: .agents/skills/nope/")
        report = self.run_validate()
        lines = [e.line for e in report.errors if "src 디렉터리가 없습니다" in e.message]
        self.assertEqual(lines, [7])

    def test_directory_src_without_trailing_slash(self):
        self.replace("sync-manifest.yml", "src: .claude/skills/demo/", "src: .claude/skills/demo")
        self.assert_error(self.run_validate(), "끝에 `/`를 붙이세요")

    def test_file_src_with_directory_dest(self):
        self.replace("sync-manifest.yml", "dest: .claude/agents/helper.md", "dest: .claude/agents/")
        self.assert_error(self.run_validate(), "dest는 `/`로 끝나면 안 됩니다")

    def test_missing_groups(self):
        self.replace("sync-manifest.yml", "    groups: [claude]\n\n  - id: codex/skills/demo", "\n  - id: codex/skills/demo")
        self.assert_error(self.run_validate(), "`groups`는 문자열 목록이어야 합니다")

    def test_duplicate_id(self):
        self.replace("sync-manifest.yml", "id: codex/skills/demo", "id: claude/skills/demo")
        self.assert_error(self.run_validate(), "id가 중복됩니다")

    def test_duplicate_dest(self):
        self.replace("sync-manifest.yml", "dest: .agents/skills/demo/", "dest: .claude/skills/demo/")
        self.assert_error(self.run_validate(), "`codex/skills/demo`: dest(.claude/skills/demo/)가 `claude/skills/demo`와 겹칩니다")

    def test_path_outside_repo(self):
        self.replace("sync-manifest.yml", "dest: .agents/skills/demo/", "dest: ../outside/")
        self.assert_error(self.run_validate(), "상대 경로여야 합니다")

    def test_versioned_item_rules(self):
        self.write(".claude/skills/_archive/demo/v1/SKILL.md", SKILL)
        self.write(".agents/skills/_archive/demo/v1/SKILL.md", SKILL)
        self.replace("sync-manifest.yml", "defaults:", textwrap.dedent("""\
              - id: claude/skills/demo@v1
                src: .claude/skills/_archive/demo/v1/
                dest: .claude/skills/demo-v1/
                groups: [claude]

              - id: codex/skills/missing@v1
                src: .agents/skills/_archive/demo/v1/
                dest: .agents/skills/missing/
                groups: []

              - id: claude/skills/demo@latest
                src: .claude/skills/demo/
                dest: .claude/skills/demo/
                groups: []

            defaults:"""))
        report = self.run_validate()
        self.assert_error(report, "`claude/skills/demo@v1`: dest가 기준 항목과 다릅니다")
        self.assert_error(report, "`claude/skills/demo@v1`: 버전 고정 항목은 `groups: []`")
        self.assert_error(report, "기준 항목 `codex/skills/missing`가 manifest에 없습니다")
        self.assert_error(report, "`claude/skills/demo@latest`: 버전 고정 id는 `<id>@v<숫자>` 형식")

    def test_unused_default_group(self):
        self.replace("sync-manifest.yml", "  - codex\n", "  - codex\n  - gemini\n")
        self.assert_error(self.run_validate(), "defaults의 그룹 `gemini`을 쓰는 항목이 없습니다")

    def test_missing_or_empty_defaults(self):
        self.replace("sync-manifest.yml", "defaults:\n  - claude\n  - codex\n", "")
        self.assert_error(self.run_validate(), "`defaults`가 없습니다")

        self.write("sync-manifest.yml", MANIFEST.replace("defaults:\n  - claude\n  - codex\n", "defaults: []\n"))
        self.assert_error(self.run_validate(), "`defaults`가 비어 있습니다")

        # 값이 빠진 `defaults:`는 None으로 읽히므로 키가 없는 것과 같이 취급한다
        self.write("sync-manifest.yml", MANIFEST.replace("defaults:\n  - claude\n  - codex\n", "defaults:\n"))
        self.assert_error(self.run_validate(), "`defaults`가 없습니다")

    def test_deletion_removes_current_item(self):
        self.replace("sync-manifest.yml", "- dest: .claude/skills/old/", "- dest: .claude/skills/demo/")
        self.assert_error(self.run_validate(), "현재 배포 항목 `claude/skills/demo`의 dest")

    def test_deletion_under_parent_dest_is_allowed(self):
        # 디스패처처럼 상위 디렉터리를 dest로 쓰는 항목이 있어도, 그 아래 경로 삭제는 정상이다
        self.replace("sync-manifest.yml", "defaults:", textwrap.dedent("""\
              - id: claude/hooks/dispatcher
                src: .claude/hooks/dispatcher/
                dest: .claude/hooks/
                groups: []

            defaults:"""))
        self.write(".claude/hooks/dispatcher/preToolUse.sh", "exit 0\n")
        self.write(".codex/hooks/dispatcher/pre-tool-use.sh", "exit 0\n")
        self.replace("sync-manifest.yml", "- dest: .claude/skills/old/", "- dest: .claude/hooks/modules/old/")
        report = self.run_validate()
        self.assertFalse(any("deletions" in e.message for e in report.errors), report.errors)

    def test_deletion_without_dest(self):
        self.replace("sync-manifest.yml", "- dest: .claude/skills/old/", "- reason: no dest")
        self.assert_error(self.run_validate(), "deletions[0]에 `dest`가 없습니다")

    def test_settings_requires_base_template(self):
        self.write(".claude/settings.json", "{}\n")
        self.replace("sync-manifest.yml", "defaults:", textwrap.dedent("""\
              - id: claude/settings
                src: .claude/settings.json
                dest: .claude/settings.json
                groups: []

            defaults:"""))
        self.assert_error(self.run_validate(), MODULE.SETTINGS_BASE)
        self.write(MODULE.SETTINGS_BASE, "{}\n")
        self.assertEqual(self.run_validate().errors, [])

    def test_invalid_manifest_yaml(self):
        self.write("sync-manifest.yml", "items: [\n")
        self.assert_error(self.run_validate(), "YAML 파싱 실패")

    # ── 미등록 ──

    def test_unregistered_units(self):
        self.write(".claude/skills/extra/SKILL.md", SKILL.replace("demo", "extra"))
        self.write(".codex/agents/_archive/helper/v1.toml", CODEX_AGENT)
        self.write(".claude/hooks/modules/new-hook/postToolUse.sh", "exit 0\n")
        report = self.run_validate()
        self.assert_error(report, ".claude/skills/extra: claude skill 'extra'가 sync-manifest.yml에 등록되지 않았습니다")
        self.assert_error(report, ".codex/agents/_archive/helper/v1.toml: codex agent 'helper/v1'가")
        self.assert_error(report, ".claude/hooks/modules/new-hook: claude hook 'new-hook'가")

    # ── 문법 ──

    def test_skill_without_skill_md(self):
        (self.root / ".agents/skills/demo/SKILL.md").unlink()
        self.write(".agents/skills/demo/notes.md", "x\n")
        self.assert_error(self.run_validate(), "codex skill 'demo'에 SKILL.md가 없습니다")

    def test_skill_frontmatter_errors(self):
        self.write(".claude/skills/demo/SKILL.md", "# no frontmatter\n")
        self.assert_error(self.run_validate(), "YAML frontmatter가 없습니다")

        self.write(".claude/skills/demo/SKILL.md", "---\nname: demo\ndescription: [broken\n---\n")
        self.assert_error(self.run_validate(), "frontmatter YAML 파싱 실패")

        self.write(".claude/skills/demo/SKILL.md", "---\nname: other\ndescription: ''\n---\n")
        report = self.run_validate()
        self.assert_error(report, "`name`(other)이 경로의 이름(demo)과 다릅니다")
        self.assert_error(report, "`description`가 비어 있거나")

    def test_archived_skill_name_uses_parent_directory(self):
        self.write(".claude/skills/_archive/demo/v1/SKILL.md", SKILL)
        self.replace("sync-manifest.yml", "defaults:", textwrap.dedent("""\
              - id: claude/skills/demo@v1
                src: .claude/skills/_archive/demo/v1/
                dest: .claude/skills/demo/
                groups: []

            defaults:"""))
        report = self.run_validate()
        self.assertEqual(report.errors, [])
        self.assert_warning(report, "claude skill 'demo/v1'에 대응하는 codex 항목이 없습니다")

    def test_claude_agent_name_mismatch(self):
        self.write(".claude/agents/helper.md", CLAUDE_AGENT.replace("name: helper", "name: helper-v2"))
        self.assert_error(self.run_validate(), "`name`(helper-v2)이 경로의 이름(helper)과 다릅니다")

    def test_codex_agent_rules(self):
        self.write(".codex/agents/helper.toml", 'name = "helper"\ndescription = [\n')
        self.assert_error(self.run_validate(), "TOML 파싱 실패")

        self.write(".codex/agents/helper.toml", textwrap.dedent("""\
            name = "helper"
            description = "Helper agent."
            model = "gpt-x"
            model_reasoning_effort = "max"
            sandbox_mode = "danger-full-access"
            developer_instructions = \"\"\"
            body
            \"\"\"
        """))
        report = self.run_validate()
        self.assert_error(report, "literal string")
        self.assert_error(report, "`model`은 생략해")
        self.assert_error(report, "`model_reasoning_effort`는")
        self.assert_error(report, "`sandbox_mode`는")

        self.write(".codex/agents/helper.toml", 'name = "helper"\ndescription = "x"\n')
        self.assert_error(self.run_validate(), "`developer_instructions`가 비어 있거나")

    def test_toml_error_reports_line(self):
        self.write(".codex/agents/helper.toml", 'name = "helper"\ndescription = "x"\nsandbox_mode = \n')
        lines = [e.line for e in self.run_validate().errors if "TOML 파싱 실패" in e.message]
        self.assertEqual(lines, [3])

    def test_toml_error_line_without_lineno_attribute(self):
        # Python 3.12의 TOMLDecodeError에는 lineno가 없고 메시지에만 위치가 들어 있다
        self.assertEqual(MODULE._toml_error_line(ValueError("Invalid value (at line 7, column 2)")), 7)
        self.assertIsNone(MODULE._toml_error_line(ValueError("Invalid value (at end of document)")))

    def test_invalid_config_toml(self):
        self.write(".codex/config.toml", 'model = "x"\n[profiles\n')
        report = self.run_validate()
        self.assert_error(report, ".codex/config.toml: TOML 파싱 실패")
        # 설정 파일에는 에이전트 규칙(`model` 금지 등)을 적용하지 않는다
        self.assertFalse(any("`model`은 생략해" in e.message for e in report.errors), report.errors)

    def test_agent_toml_is_parsed_once(self):
        self.write(".codex/agents/helper.toml", 'name = "helper"\ndescription = [\n')
        errors = [e for e in self.run_validate().errors if "TOML 파싱 실패" in e.message]
        self.assertEqual(len(errors), 1, errors)

    def test_invalid_json(self):
        self.write(".gemini/settings.json", '{"a": 1,}\n')
        self.assert_error(self.run_validate(), ".gemini/settings.json: JSON 파싱 실패")

    # ── Claude/Codex 대응 ──

    def test_missing_counterpart_is_warning(self):
        (self.root / ".codex/agents/helper.toml").unlink()
        self.replace(
            "sync-manifest.yml",
            "  - id: codex/agents/helper\n"
            "    src: .codex/agents/helper.toml\n"
            "    dest: .codex/agents/helper.toml\n"
            "    groups: [codex]\n\n",
            "",
        )
        report = self.run_validate()
        self.assertEqual(report.errors, [])
        self.assert_warning(report, "claude agent 'helper'에 대응하는 codex 항목이 없습니다")

    def test_hook_event_mismatch_is_warning(self):
        self.write(".claude/hooks/modules/guard/postToolUse.sh", "exit 0\n")
        report = self.run_validate()
        self.assertEqual(report.errors, [])
        self.assert_warning(report, "claude만: ['post-tool-use.sh']")

    def test_one_sided_change_is_warning(self):
        report = self.run_validate(changed=[".claude/skills/demo/SKILL.md", ".claude/agents/helper.md",
                                            ".codex/agents/helper.toml"])
        self.assertEqual(report.errors, [])
        self.assert_warning(report, "claude skill 'demo'만 수정되고 codex 쪽(.agents/skills/demo)은 수정되지 않았습니다")
        self.assertFalse(any("helper" in w.message for w in report.warnings), report.warnings)

    def test_both_sides_changed_has_no_warning(self):
        report = self.run_validate(changed=[".claude/skills/demo/SKILL.md", ".agents/skills/demo/references/x.md"])
        self.assertEqual(report.warnings, [])


class ChangedFilesTest(unittest.TestCase):
    def setUp(self):
        self._tmp = tempfile.TemporaryDirectory()
        self.root = pathlib.Path(self._tmp.name)
        self.git("init", "-q", "-b", "main")
        self.git("config", "user.email", "test@example.com")
        self.git("config", "user.name", "test")
        self.write("a.txt", "a\n")
        self.git("add", ".")
        self.git("commit", "-q", "-m", "base")

    def tearDown(self):
        self._tmp.cleanup()

    def git(self, *args):
        subprocess.run(["git", *args], cwd=self.root, check=True, capture_output=True)

    def write(self, rel, content):
        path = self.root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8")

    def test_includes_committed_working_tree_and_untracked_changes(self):
        self.git("switch", "-q", "-c", "feature")
        self.write("committed.txt", "c\n")
        self.git("add", ".")
        self.git("commit", "-q", "-m", "change")
        self.write("a.txt", "modified\n")
        self.write("staged.txt", "s\n")
        self.git("add", "staged.txt")
        self.write("untracked.txt", "u\n")
        self.assertEqual(
            MODULE.changed_files(self.root, "main"),
            ["a.txt", "committed.txt", "staged.txt", "untracked.txt"],
        )

    def test_ignores_commits_added_to_base_after_branching(self):
        self.git("switch", "-q", "-c", "feature")
        self.write("feature.txt", "f\n")
        self.git("add", ".")
        self.git("commit", "-q", "-m", "feature")
        self.git("switch", "-q", "main")
        self.write("main-only.txt", "m\n")
        self.git("add", ".")
        self.git("commit", "-q", "-m", "main")
        self.git("switch", "-q", "feature")
        self.assertEqual(MODULE.changed_files(self.root, "main"), ["feature.txt"])

    def test_unknown_base_raises(self):
        with self.assertRaises(RuntimeError):
            MODULE.changed_files(self.root, "does-not-exist")


class PrintTest(unittest.TestCase):
    def test_github_annotation_is_single_line(self):
        finding = MODULE.Finding("sync-manifest.yml", "YAML 파싱 실패: while parsing\r\n  in line 1\n100% 확인", 1)
        out = io.StringIO()
        with contextlib.redirect_stdout(out):
            MODULE._print([finding], "error", github=True)
        self.assertEqual(
            out.getvalue(),
            "::error file=sync-manifest.yml,line=1::YAML 파싱 실패: while parsing%0A  in line 1%0A100%25 확인\n",
        )


class RepositoryTest(unittest.TestCase):
    def test_repository_passes_validation(self):
        report = MODULE.validate(MODULE.ROOT)
        self.assertEqual([f"{e.path}: {e.message}" for e in report.errors], [])


if __name__ == "__main__":
    unittest.main()
