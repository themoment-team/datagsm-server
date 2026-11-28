#!/usr/bin/env python3
"""
sync-manifest.yml의 deletions 목록에 따라 타깃 레포에서 구 파일/디렉터리를 삭제합니다.
열려 있는 sync PR 브랜치에 삭제 커밋을 얹거나, 없으면 새 브랜치 + PR을 생성합니다.

사용법:
  GH_TOKEN=<token> python3 scripts/apply-deletions.py \
    --repo owner/repo \
    --branch-prefix harness-sync/ \
    --base-branch main
"""
from __future__ import annotations

import argparse
import os
import subprocess
import sys
import tempfile
from pathlib import Path

import yaml

MANIFEST_PATH = Path(__file__).parent.parent / "sync-manifest.yml"
BOT_EMAIL = "github-actions[bot]@users.noreply.github.com"
BOT_NAME = "github-actions[bot]"


def run(cmd: list[str], cwd: str | None = None, check: bool = True) -> subprocess.CompletedProcess:
    return subprocess.run(cmd, text=True, capture_output=True, cwd=cwd, check=check)


def git(tmpdir: str, *args: str, check: bool = True) -> subprocess.CompletedProcess:
    return run(["git", "-C", tmpdir, *args], check=check)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--repo", required=True)
    parser.add_argument("--branch-prefix", required=True)
    parser.add_argument("--base-branch", required=True)
    parser.add_argument(
        "--pr-label",
        default="harness sync:하네스 동기화",
        help="cleanup PR에 붙일 라벨. 빈 문자열이면 라벨을 붙이지 않는다.",
    )
    args = parser.parse_args()

    with open(MANIFEST_PATH) as f:
        manifest = yaml.safe_load(f)

    deletions: list[str] = [
        entry["dest"] for entry in manifest.get("deletions", [])
    ]
    if not deletions:
        print("No deletions configured, skipping.")
        return

    gh_token = os.environ.get("GH_TOKEN", "")
    if not gh_token:
        print("Error: GH_TOKEN not set", file=sys.stderr)
        sys.exit(1)

    repo = args.repo
    sync_branch = f"{args.branch_prefix}default"
    base_branch = args.base_branch
    clone_url = f"https://x-access-token:{gh_token}@github.com/{repo}.git"

    with tempfile.TemporaryDirectory() as tmpdir:
        print(f"Cloning {repo}...")
        result = run(["git", "clone", "--quiet", clone_url, tmpdir])
        if result.returncode != 0:
            print(f"Clone failed: {result.stderr}", file=sys.stderr)
            sys.exit(1)

        git(tmpdir, "config", "user.email", BOT_EMAIL)
        git(tmpdir, "config", "user.name", BOT_NAME)

        branch_exists = (
            git(tmpdir, "ls-remote", "--exit-code", "origin", sync_branch, check=False).returncode == 0
        )

        if branch_exists:
            git(tmpdir, "checkout", sync_branch)
            print(f"Checked out existing branch: {sync_branch}")
        else:
            base_ref = f"origin/{base_branch}"
            result = git(tmpdir, "checkout", "-b", sync_branch, base_ref, check=False)
            if result.returncode != 0:
                print(f"Failed to create branch from {base_ref}: {result.stderr}", file=sys.stderr)
                sys.exit(1)
            print(f"Created new branch: {sync_branch} from {base_branch}")

        changed = False
        for dest in deletions:
            dest_clean = dest.rstrip("/")
            target = Path(tmpdir) / dest_clean
            if target.exists():
                git(tmpdir, "rm", "-rf", dest_clean)
                changed = True
                print(f"Deleted: {dest_clean}")
            else:
                print(f"Not found (already clean): {dest_clean}")

        if not changed:
            print(f"No orphaned files to clean in {repo}")
            return

        git(tmpdir, "commit", "-m", "chore: remove orphaned files from previous sync")

        result = git(tmpdir, "push", "origin", sync_branch, check=False)
        if result.returncode != 0:
            print(f"Push failed: {result.stderr}", file=sys.stderr)
            sys.exit(1)

        gh_env = {**os.environ, "GH_TOKEN": gh_token}

        existing_pr = subprocess.run(
            ["gh", "pr", "list", "--repo", repo, "--head", sync_branch,
             "--json", "number", "--jq", ".[0].number"],
            text=True, capture_output=True, env=gh_env, check=False,
        ).stdout.strip()

        if not existing_pr:
            create_cmd = [
                "gh", "pr", "create",
                "--repo", repo,
                "--head", sync_branch,
                "--base", base_branch,
                "--title", "chore: remove orphaned files from previous sync",
                "--body", "Automated cleanup: removes files renamed or deleted in themoment-ai-harness-sync.",
            ]
            if args.pr_label:
                create_cmd += ["--label", args.pr_label]
            result = subprocess.run(
                create_cmd,
                text=True, capture_output=True, env=gh_env, check=False,
            )
            if result.returncode == 0:
                print(f"Created cleanup PR in {repo}")
            else:
                print(f"PR creation failed (label may not exist): {result.stderr}", file=sys.stderr)
        else:
            print(f"Cleanup commit added to existing PR #{existing_pr} in {repo}")


if __name__ == "__main__":
    main()