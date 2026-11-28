#!/usr/bin/env python3
"""Sync 워크플로우 결과를 취합해 GitHub Actions 잡 요약으로 렌더링한다.

각 매트릭스 레그가 업로드한 result-*.json 을 읽어 통계와 실패 내역을
`$GITHUB_STEP_SUMMARY` 에 마크다운으로 출력한다.

- 대표적이고 명확한 오류(base_branch 없음, 토큰 실패 등)는 레포 링크와 함께
  분명한 한 줄 메시지로 표시한다.
- 그 외 알 수 없는 오류는 해당 잡의 원시 로그를 함께 첨부한다.

env:
  RESULTS_DIR     result-*.json 이 있는 디렉터리 (기본 results)
  STATS           prepare 잡이 출력한 통계 JSON
  PREPARE_RESULT  prepare 잡 result
  SYNC_RESULT     sync 잡 result
  GH_REPO         소스 레포 (github.repository) — 잡/로그 API 조회용
  RUN_ID          github.run_id
  SERVER_URL      github.server_url
  GH_TOKEN        actions:read 권한 토큰 (github.token)
  GITHUB_STEP_SUMMARY  요약 출력 파일 경로
"""
import glob
import json
import os
import subprocess
import sys

RESULTS_DIR = os.environ.get("RESULTS_DIR", "results")
GH_REPO = os.environ.get("GH_REPO", "")
RUN_ID = os.environ.get("RUN_ID", "")
SERVER_URL = os.environ.get("SERVER_URL", "https://github.com")
PREPARE_RESULT = os.environ.get("PREPARE_RESULT", "")
SYNC_RESULT = os.environ.get("SYNC_RESULT", "")


def load_stats() -> dict:
    raw = os.environ.get("STATS", "")
    try:
        return json.loads(raw) if raw else {}
    except (ValueError, TypeError):
        return {}


def load_results() -> list[dict]:
    results = []
    for path in sorted(glob.glob(os.path.join(RESULTS_DIR, "result-*.json"))):
        try:
            with open(path, encoding="utf-8") as f:
                results.append(json.load(f))
        except (OSError, ValueError):
            pass
    return results


def fetch_jobs() -> dict:
    """잡 이름 → {id, url} 매핑. continue-on-error가 conclusion을 감추므로
    상태 판정엔 쓰지 않고, 실패 레그의 원시 로그/링크를 찾는 데만 쓴다."""
    if not GH_REPO or not RUN_ID:
        return {}
    try:
        result = subprocess.run(
            [
                "gh", "api",
                f"/repos/{GH_REPO}/actions/runs/{RUN_ID}/jobs?per_page=100",
                "--paginate",
                "--jq", ".jobs[] | {name: .name, id: .id, url: .html_url}",
            ],
            capture_output=True,
            text=True,
            errors="replace",
        )
    except (FileNotFoundError, subprocess.SubprocessError):
        return {}
    jobs = {}
    if result.returncode == 0:
        for line in result.stdout.splitlines():
            line = line.strip()
            if not line:
                continue
            try:
                obj = json.loads(line)
                jobs[obj["name"]] = {"id": obj["id"], "url": obj["url"]}
            except (ValueError, KeyError):
                pass
    return jobs


def fetch_log_tail(job_id: int, max_chars: int = 12000) -> str:
    if not GH_REPO:
        return ""
    try:
        result = subprocess.run(
            ["gh", "api", f"/repos/{GH_REPO}/actions/jobs/{job_id}/logs"],
            capture_output=True,
            text=True,
            errors="replace",
        )
    except (FileNotFoundError, subprocess.SubprocessError):
        return ""
    if result.returncode != 0 or not result.stdout:
        return ""
    log = result.stdout
    if len(log) > max_chars:
        log = "... (앞부분 생략) ...\n" + log[-max_chars:]
    return log


def main() -> None:
    stats = load_stats()
    results = load_results()

    successes = [r for r in results if r.get("status") == "success"]
    skipped = [r for r in results if r.get("status") == "skipped"]
    failures = [r for r in results if r.get("status") == "failed"]

    known = [f for f in failures if f.get("error_class") != "sync-action-failed"]
    unknown = [f for f in failures if f.get("error_class") == "sync-action-failed"]

    lines: list[str] = []
    lines.append("# AI Harness Sync 결과")
    lines.append("")

    # 한눈 요약 표
    lines.append("| 지표 | 값 |")
    lines.append("| --- | --- |")
    lines.append(f"| Prepare / Sync | `{PREPARE_RESULT}` / `{SYNC_RESULT}` |")
    if stats:
        lines.append(
            f"| 동기화 시도 | **{stats.get('attempted', 0)} / "
            f"{stats.get('total_repos', 0)}** 레포 (설치 {stats.get('installations', 0)}개) |"
        )
        lines.append(
            f"| 건너뜀 | 동기화 비활성화 {stats.get('skipped_disabled', 0)} · "
            f"열린 PR {stats.get('skipped_open_pr', 0)} · "
            f"대상 파일 없음 {stats.get('skipped_no_files', 0)} · "
            f"설정 조회 실패 {stats.get('skipped_config_error', 0)} |"
        )
    fail_cell = f"**{len(failures)}**" if failures else "0"
    lines.append(
        f"| 성공 · 건너뜀 · 실패 | {len(successes)} · {len(skipped)} · {fail_cell} |"
    )
    lines.append("")

    # prepare 자체가 실패한 경우(매트릭스 산출 크래시 등)
    if PREPARE_RESULT == "failure":
        lines.append("## 🚨 prepare 단계 실패")
        lines.append("")
        lines.append("매트릭스 산출 단계가 실패해 이번 실행은 동기화를 진행하지 못했습니다.")
        lines.append("")
        info = fetch_jobs().get("prepare")
        if info:
            log = fetch_log_tail(info["id"])
            if log:
                lines.append(f"<details><summary>로그 원문 · <a href=\"{info['url']}\">전체 로그</a></summary>")
                lines.append("")
                lines.append("```text")
                lines.append(log.rstrip())
                lines.append("```")
                lines.append("")
                lines.append("</details>")
                lines.append("")

    if failures:
        lines.append("## ❌ 실패한 레포")
        lines.append("")
        # 실패 목록은 표 한 장으로 (알 수 없는 오류는 로그 링크로 연결)
        lines.append("| 레포 | 원인 |")
        lines.append("| --- | --- |")
        for f in known:
            repo = f.get("repo", "?")
            lines.append(f"| [`{repo}`]({f.get('repo_url', '')}) | {f.get('message', '')} |")
        for f in unknown:
            repo = f.get("repo", "?")
            lines.append(f"| [`{repo}`]({f.get('repo_url', '')}) | 알 수 없는 오류 — 아래 로그 참조 |")
        lines.append("")

        # 알 수 없는 오류만 원시 로그를 접이식으로 첨부
        if unknown:
            jobs = fetch_jobs()
            for f in unknown:
                repo = f.get("repo", "?")
                info = jobs.get(f.get("job_name", ""))
                job_url = info["url"] if info else f.get("repo_url", "")
                log = fetch_log_tail(info["id"]) if info else ""
                if log:
                    lines.append(
                        f"<details><summary><code>{repo}</code> 로그 원문 · "
                        f"<a href=\"{job_url}\">전체 로그</a></summary>"
                    )
                    lines.append("")
                    lines.append("```text")
                    lines.append(log.rstrip())
                    lines.append("```")
                    lines.append("")
                    lines.append("</details>")
                    lines.append("")
                else:
                    lines.append(f"- [`{repo}`]({job_url}) — 전체 job 로그에서 원인을 확인하세요")
                    lines.append("")

    config_errors = stats.get("skipped_config_error", 0) if stats else 0
    if config_errors:
        lines.append(
            f"⚠️ `.harness/sync.yml` 조회 실패로 {config_errors}개 레포가 이번 회차에서 건너뛰었습니다. "
            "원인은 prepare 잡의 annotation에서 확인하세요. 다음 실행에서 다시 시도합니다."
        )
        lines.append("")
    elif not failures and PREPARE_RESULT != "failure":
        lines.append("모든 동기화가 문제없이 처리되었습니다.")
        lines.append("")

    report = "\n".join(lines) + "\n"

    summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary_path:
        with open(summary_path, "a", encoding="utf-8") as f:
            f.write(report)
    else:
        # 로컬 실행 등 요약 파일이 없으면 stdout으로
        sys.stdout.write(report)


if __name__ == "__main__":
    try:
        main()
    except Exception as e:  # 보고는 best-effort — 잡을 실패시키지 않는다
        print(f"render-sync-report 실패(무시): {e}", file=sys.stderr)
