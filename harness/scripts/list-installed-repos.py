#!/usr/bin/env python3
"""
GitHub App이 설치된 모든 레포를 installation 단위로 조회해서
GitHub Actions matrix용 JSON을 stdout으로 출력합니다.

출력 형식:
  [{"id": 12345, "config": "group:\n  - repos: |..."}, ...]

각 레포 루트의 .harness/sync.yml을 읽어 동기화할 항목을 결정합니다.
파일이 없으면 sync-manifest.yml의 defaults 그룹을 모두 적용합니다. 조회 오류나
파싱 오류가 발생한 레포는 이번 실행에서 건너뜁니다.

.harness/sync.yml 형식:
  enabled:       # 이 레포의 동기화 여부 (기본값: true)
    false        # false면 매트릭스에서 제외되어 아무 작업도 하지 않는다
  groups:        # 포함할 그룹 (기본값: defaults 그룹 전체)
    - claude
    - codex
  exclude:       # groups에서 제외할 항목 ID
    - claude/skills/kotest-guide
  include:       # groups 외에 추가할 항목 ID
    - claude/hooks/ktlint
  overrides:     # 항목별 true/false 또는 버전 문자열("v1") 지정
    claude/skills/git-commit: false
  pr_label:      # sync PR에 'harness sync:하네스 동기화' 라벨을 붙일지 (기본값: true)
    false

환경변수:
  APP_ID           - GitHub App ID
  APP_PRIVATE_KEY  - GitHub App Private Key (.pem 전체 내용)
  SOURCE_REPO      - 제외할 소스 레포 (기본값: themoment-team/themoment-ai-harness-sync)
"""
from __future__ import annotations

import base64
import io
import json
import os
import subprocess
import sys
import time
import urllib.error
import urllib.request
from collections import defaultdict
from pathlib import Path

import yaml


MANIFEST_PATH = Path(__file__).parent.parent / "sync-manifest.yml"
GH_API_BASE = "https://api.github.com"

# sync PR에 기본으로 붙는 라벨. .harness/sync.yml의 pr_label: false 로 비활성화 가능.
SYNC_PR_LABEL = "harness sync:하네스 동기화"


def load_manifest() -> dict:
    with open(MANIFEST_PATH) as f:
        return yaml.safe_load(f)


def generate_jwt(app_id: str, private_key: str) -> str:
    import jwt

    now = int(time.time())
    payload = {"iat": now - 60, "exp": now + 600, "iss": app_id}
    return jwt.encode(payload, private_key, algorithm="RS256")


def app_api(endpoint: str, app_jwt: str, method: str = "GET") -> any:
    """App JWT 인증이 필요한 엔드포인트 호출 (Bearer 스킴 사용)."""
    results = []
    url = f"{GH_API_BASE}{endpoint}"
    while url:
        req = urllib.request.Request(
            url,
            data=b"" if method == "POST" else None,
            method=method,
            headers={
                "Authorization": f"Bearer {app_jwt}",
                "Accept": "application/vnd.github+json",
                "X-GitHub-Api-Version": "2022-11-28",
            },
        )
        with urllib.request.urlopen(req) as resp:
            data = json.loads(resp.read())
            link_header = resp.headers.get("Link", "")
            if isinstance(data, list):
                results.extend(data)
            else:
                return data
            next_url = None
            for part in link_header.split(","):
                if 'rel="next"' in part:
                    next_url = part.strip().split(";")[0].strip().strip("<>")
                    break
            url = next_url
    return results


def get_installation_token(inst_id: int, app_jwt: str) -> str:
    data = app_api(f"/app/installations/{inst_id}/access_tokens", app_jwt, method="POST")
    return data["token"]


def gh_api(endpoint: str, token: str) -> any:
    """Installation token 인증이 필요한 엔드포인트 호출 (gh CLI 사용)."""
    result = subprocess.run(
        ["gh", "api", endpoint, "--paginate", "--slurp"],
        env={**os.environ, "GH_TOKEN": token},
        capture_output=True,
        text=True,
        check=True,
    )
    pages = json.loads(result.stdout)
    if not pages:
        return []
    if isinstance(pages[0], list):
        return [item for page in pages for item in page]
    data = pages[0]
    if "repositories" in data:
        data["repositories"] = [repo for page in pages for repo in page["repositories"]]
    return data


class ConfigFetchError(Exception):
    """레포별 sync.yml 조회 또는 파싱 실패."""


def get_repo_harness_config(full_name: str, token: str) -> dict | None:
    result = subprocess.run(
        ["gh", "api", f"/repos/{full_name}/contents/.harness/sync.yml"],
        env={**os.environ, "GH_TOKEN": token},
        capture_output=True,
        text=True,
    )
    if result.returncode != 0:
        # gh 상태 줄(`gh: <message> (HTTP <status>)`)로만 판정해 부가 메시지 속 404 문자열에 속지 않는다
        lines = result.stderr.splitlines()
        status_line = next((line for line in lines if line.startswith("gh: ")), "")
        if status_line.endswith("(HTTP 404)"):
            return None
        detail = " / ".join(lines[:3]) or "알 수 없는 오류"
        raise ConfigFetchError(f"{full_name}: gh api 실패(exit {result.returncode}): {detail}")
    try:
        data = json.loads(result.stdout)
        content = base64.b64decode(data["content"]).decode()
    except (ValueError, KeyError, TypeError, UnicodeDecodeError) as e:
        raise ConfigFetchError(f"{full_name}: sync.yml 응답 파싱 실패 ({type(e).__name__})") from e
    try:
        config = yaml.safe_load(content)
    except yaml.YAMLError as e:
        raise ConfigFetchError(f"{full_name}: sync.yml YAML 파싱 실패 ({type(e).__name__})") from e
    if config is None:
        return {}
    if not isinstance(config, dict):
        raise ConfigFetchError(f"{full_name}: sync.yml 루트는 mapping이어야 합니다")
    return config


def select_target_repositories(repositories: list[dict], target_repo: str | None) -> list[dict]:
    if not target_repo:
        return repositories
    return [repository for repository in repositories if repository["full_name"] == target_repo]


def sync_branch_name(branch_prefix: str, base_branch: str) -> str:
    """repo-file-sync-action이 생성하는 sync 브랜치 이름.

    액션은 `path.join(BRANCH_PREFIX, repo.branch)` 로 브랜치를 만든다.
    repo.branch 는 repo 식별자의 '@<branch>' (= base_branch)다.
    예: prefix='update/', base='develop' → 'update/develop'
        prefix='harness-sync/', base='main' → 'harness-sync/main'
    """
    return f"{branch_prefix.rstrip('/')}/{base_branch}"


def has_open_sync_pr(full_name: str, branch_prefix: str, base_branch: str, token: str) -> bool:
    """이미 열린 sync PR(head ref가 정확히 sync 브랜치)이 있는지 확인.

    있으면 동기화 대상에서 제외한다. repo-file-sync-action이 기존 sync 브랜치를
    재사용하면서 동일 내용을 force-push하는 churn을 막기 위함.
    PR이 머지/종료되면 다시 동기화 대상이 된다.

    head ref를 prefix가 아닌 정확한 sync 브랜치 이름으로 비교해야 한다.
    (사람이 만든 '<prefix>...' 작업 브랜치 PR과 충돌하면 안 되므로)
    """
    branch = sync_branch_name(branch_prefix, base_branch)
    # --jq로 head.ref만 뽑아 줄 단위로 받는다. PR 본문/제목에 '][' 같은
    # 문자열이 들어가도 임의의 JSON을 직접 파싱하지 않으므로 안전하다.
    result = subprocess.run(
        [
            "gh", "api",
            f"/repos/{full_name}/pulls?state=open&per_page=100",
            "--paginate",
            "--jq", ".[].head.ref",
        ],
        env={**os.environ, "GH_TOKEN": token},
        capture_output=True,
        text=True,
    )
    if result.returncode != 0:
        # 조회 실패 시 보수적으로 '없음' 처리하여 동기화는 진행
        return False
    return any(
        line.strip() == branch
        for line in result.stdout.splitlines()
    )


SETTINGS_WITH_HOOKS = ".claude/settings.json"
SETTINGS_BASE = ".claude/templates/settings-base.json"


def resolve_files(
    config: dict | None,
    manifest: dict,
    default_groups: list[str],
) -> list[tuple[str, str]]:
    all_items: dict[str, tuple[str, str]] = {
        item["id"]: (item["src"], item["dest"])
        for item in manifest["items"]
    }
    item_groups: dict[str, list[str]] = {
        item["id"]: item["groups"]
        for item in manifest["items"]
    }

    if config is None:
        selected_groups = default_groups
        overrides: dict[str, bool] = {}
    else:
        selected_groups = config.get("groups", default_groups)
        overrides = config.get("overrides", {})
        # 하위 호환: 구형 exclude/include 형식 지원
        if not overrides and ("exclude" in config or "include" in config):
            for item_id in config.get("exclude", []):
                overrides[item_id] = False
            for item_id in config.get("include", []):
                overrides[item_id] = True

    selected: dict[str, tuple[str, str]] = {}

    for item_id, groups in item_groups.items():
        if any(g in selected_groups for g in groups):
            selected[item_id] = all_items[item_id]

    for item_id, override in overrides.items():
        if isinstance(override, str):
            versioned_id = f"{item_id}@{override}"
            if versioned_id in all_items:
                canonical_dest = all_items.get(item_id, all_items[versioned_id])[1]
                selected[item_id] = (all_items[versioned_id][0], canonical_dest)
            else:
                print(
                    f"Warning: {versioned_id} not in manifest, skipping pin",
                    file=sys.stderr,
                )
        elif override:
            if item_id in all_items:
                selected[item_id] = all_items[item_id]
        else:
            selected.pop(item_id, None)

    # hooks 포함 여부에 따라 settings.json 소스 파일 분기
    if "claude/settings" in selected:
        has_hooks = any(item_id.startswith("claude/hooks/") for item_id in selected)
        dest = selected["claude/settings"][1]
        selected["claude/settings"] = (
            SETTINGS_WITH_HOOKS if has_hooks else SETTINGS_BASE,
            dest,
        )

    return list(selected.values())


def build_sync_config(full_name: str, files: list[tuple[str, str]], base_branch: str) -> str:
    """BetaHuhn/repo-file-sync-action용 YAML 설정 문자열을 생성합니다 (레포 1개).

    repo 식별자에 '@<base_branch>'를 붙여 액션이 sync 브랜치를 base_branch에서
    분기하고 PR도 그 브랜치로 보내도록 한다. 이걸 지정하지 않으면 액션이 레포의
    GitHub 기본 브랜치(예: master)에서 분기하는데, base_branch(develop)가 기본
    브랜치보다 앞서 있으면 이미 동기화된 내용이 phantom diff로 PR에 올라온다.
    """
    if not files:
        return "group: []"

    buf = io.StringIO()
    buf.write("group:\n")
    buf.write("  - repos: |\n")
    buf.write(f"      {full_name}@{base_branch}\n")
    buf.write("    files:\n")
    for src, dest in files:
        buf.write(f"      - source: {src}\n")
        buf.write(f"        dest: {dest}\n")
    return buf.getvalue()


def main():
    app_id = os.environ.get("APP_ID")
    private_key = os.environ.get("APP_PRIVATE_KEY")
    source_repo = os.environ.get("SOURCE_REPO", "themoment-team/themoment-ai-harness-sync")
    target_repo = os.environ.get("TARGET_REPO")

    if not app_id or not private_key:
        print("Error: APP_ID and APP_PRIVATE_KEY must be set", file=sys.stderr)
        sys.exit(1)

    manifest = load_manifest()
    default_groups: list[str] = manifest.get("defaults", [])

    app_jwt = generate_jwt(app_id, private_key)

    installations = app_api("/app/installations", app_jwt)
    if not isinstance(installations, list):
        installations = [installations]

    # 레포 단위 matrix (per-repo per-installation)
    matrix: list[dict] = []

    # 보고서용 통계
    total_repos = 0        # source repo 제외, 설치에 연결된 전체 레포 수
    skipped_disabled = 0   # enabled: false 로 동기화를 끈 수
    skipped_open_pr = 0    # 열린 sync PR이 있어 건너뛴 수
    skipped_no_files = 0   # 대상 파일이 없어 건너뛴 수
    skipped_config_error = 0  # sync.yml 조회/파싱 실패로 건너뛴 수
    target_repo_found = False
    target_config_error = False  # 수동 지정한 레포가 설정 조회 실패로 건너뛰어졌는지

    for inst in installations:
        inst_id = inst["id"]
        try:
            inst_token = get_installation_token(inst_id, app_jwt)
        except urllib.error.HTTPError as e:
            print(f"Warning: failed to get token for installation {inst_id}: {e}", file=sys.stderr)
            continue

        data = gh_api("/installation/repositories", inst_token)
        repos = data if isinstance(data, list) else data.get("repositories", [])
        repos = select_target_repositories(repos, target_repo)

        for repo in repos:
            full_name = repo["full_name"]
            target_repo_found = target_repo_found or full_name == target_repo
            if full_name == source_repo:
                continue

            total_repos += 1
            default_branch = repo.get("default_branch", "main")
            try:
                config = get_repo_harness_config(full_name, inst_token)
            except ConfigFetchError as e:
                skipped_config_error += 1
                target_config_error = target_config_error or full_name == target_repo
                print(
                    f"  [{inst_id}] {full_name}: skip — sync.yml 조회 실패 ({e})",
                    file=sys.stderr,
                )
                # 실행 화면에 annotation으로 남겨 prepare 로그를 열지 않아도 보이게 한다
                print(f"::warning title=sync.yml 조회 실패::{e}", file=sys.stderr)
                continue

            # enabled: false 면 이 레포는 동기화 대상에서 완전히 제외한다.
            # 설정 파일이 없거나 키가 없으면 기본값 true.
            if config is not None and not config.get("enabled", True):
                skipped_disabled += 1
                print(
                    f"  [{inst_id}] {full_name}: skip — enabled: false (동기화 비활성화)",
                    file=sys.stderr,
                )
                continue

            branch_prefix = "harness-sync/"
            base_branch = default_branch
            if config:
                branch_prefix = config.get("branch_prefix", "harness-sync/")
                base_branch = config.get("base_branch", default_branch)
                overrides_log = config.get("overrides") or {
                    k: False for k in config.get("exclude", [])
                } | {k: True for k in config.get("include", [])}
                print(
                    f"  [{inst_id}] {full_name}: groups={config.get('groups')} "
                    f"overrides={overrides_log} "
                    f"branch_prefix={branch_prefix} base_branch={base_branch}",
                    file=sys.stderr,
                )
            else:
                print(
                    f"  [{inst_id}] {full_name}: no config, using defaults={default_groups} "
                    f"base_branch={base_branch}",
                    file=sys.stderr,
                )

            files = resolve_files(config, manifest, default_groups)
            if not files:
                skipped_no_files += 1
                continue

            # 이미 열린 sync PR이 있으면 건너뛴다 (phantom force-push churn 방지).
            # PR이 머지/종료된 뒤 다음 실행에서 최신 내용으로 다시 동기화된다.
            if has_open_sync_pr(full_name, branch_prefix, base_branch, inst_token):
                skipped_open_pr += 1
                print(
                    f"  [{inst_id}] {full_name}: skip — 열린 sync PR 존재 "
                    f"(branch_prefix={branch_prefix})",
                    file=sys.stderr,
                )
                continue

            language = "en"
            pr_label_enabled = True
            if config:
                language = config.get("language", "en")
                pr_label_enabled = config.get("pr_label", True)

            matrix.append({
                "inst_id": inst_id,
                "repo": full_name,
                "branch_prefix": branch_prefix,
                "base_branch": base_branch,
                "language": language,
                "pr_labels": SYNC_PR_LABEL if pr_label_enabled else "",
                "config": build_sync_config(full_name, files, base_branch),
            })

    if target_repo and not target_repo_found:
        print(f"Error: target repository is not installed: {target_repo}", file=sys.stderr)
        sys.exit(1)
    # 수동 실행은 그 레포를 동기화하려는 명시적 요청이므로 조용히 성공 처리하지 않는다
    if target_repo and target_config_error:
        print(f"Error: failed to read .harness/sync.yml of target repository: {target_repo}", file=sys.stderr)
        sys.exit(1)

    stats = {
        "installations": len(installations),
        "total_repos": total_repos,
        "attempted": len(matrix),
        "skipped_disabled": skipped_disabled,
        "skipped_open_pr": skipped_open_pr,
        "skipped_no_files": skipped_no_files,
        "skipped_config_error": skipped_config_error,
    }
    print(
        f"  stats: attempted={stats['attempted']}/{total_repos} repos "
        f"(installations={stats['installations']}, "
        f"skip_disabled={skipped_disabled}, "
        f"skip_open_pr={skipped_open_pr}, skip_no_files={skipped_no_files}, "
        f"skip_config_error={skipped_config_error})",
        file=sys.stderr,
    )
    gh_output = os.environ.get("GITHUB_OUTPUT")
    if gh_output:
        with open(gh_output, "a") as f:
            f.write(f"stats={json.dumps(stats)}\n")

    print(json.dumps(matrix))


if __name__ == "__main__":
    main()
