#!/usr/bin/env python3
"""
sync-manifest.yml과 배포 대상 파일을 검사합니다. PR 검증 워크플로와 로컬에서 같이 씁니다.

에러 (exit 1):
  - manifest 형식: 필수 키, id 중복, 버전 고정 항목(`<id>@vN`) 규칙, defaults, deletions
  - 스킬·에이전트 항목의 domain(frontend / backend / common)
  - src 경로가 실제로 있는지, 디렉터리/파일 구분(끝의 `/`)이 맞는지
  - manifest에 등록되지 않은 스킬·에이전트·훅 모듈
  - SKILL.md와 Claude 에이전트 frontmatter, Codex 에이전트 TOML, JSON 설정 문법

경고 (exit 0):
  - Claude/Codex 한쪽에만 있는 스킬·에이전트·훅 모듈
  - --base를 주면, 그 커밋과 갈라진 뒤 한쪽만 수정된 스킬·에이전트·훅 모듈 (커밋하지 않은 변경 포함)

사용법:
  python3 scripts/validate-manifest.py
  python3 scripts/validate-manifest.py --base origin/develop
"""
from __future__ import annotations

import argparse
import importlib.util
import json
import os
import re
import subprocess
import sys
import tomllib
from dataclasses import dataclass, field
from pathlib import Path, PurePosixPath

import yaml


ROOT = Path(__file__).resolve().parent.parent
MANIFEST_NAME = "sync-manifest.yml"

# settings.json 소스 분기는 동기화 스크립트의 상수를 그대로 쓴다
_SYNC_SPEC = importlib.util.spec_from_file_location(
    "list_installed_repos", Path(__file__).with_name("list-installed-repos.py")
)
assert _SYNC_SPEC and _SYNC_SPEC.loader
_SYNC = importlib.util.module_from_spec(_SYNC_SPEC)
_SYNC_SPEC.loader.exec_module(_SYNC)
SETTINGS_BASE: str = _SYNC.SETTINGS_BASE

VERSIONED_ID = re.compile(r"^(?P<base>[^@]+)@(?P<version>v\d+)$")
DOMAIN_REQUIRED_ID = re.compile(r"^[^/]+/(skills|agents)/")
DOMAINS = {"frontend", "backend", "common"}
FRONTMATTER = re.compile(r"^---\r?\n(.*?)\r?\n---\r?\n", re.S)
TOML_LITERAL_INSTRUCTIONS = re.compile(r"^developer_instructions\s*=\s*'''", re.M)
TOML_ERROR_LINE = re.compile(r"\(at line (\d+), column \d+\)")

CODEX_AGENT_REQUIRED = ("name", "description", "developer_instructions")
CODEX_REASONING_EFFORTS = {"low", "medium", "high"}
CODEX_SANDBOX_MODES = {"read-only", "workspace-write"}

# Claude 훅 이벤트 파일 → Codex 훅 이벤트 파일
HOOK_EVENT_FILES = {
    "preToolUse.sh": "pre-tool-use.sh",
    "postToolUse.sh": "post-tool-use.sh",
    "sessionStart.sh": "session-start.sh",
}

# JSON·TOML 설정 문법을 검사할 배포 디렉터리
CONFIG_ROOTS = (".claude", ".codex", ".gemini", ".agents")


@dataclass
class Finding:
    path: str
    message: str
    line: int | None = None


@dataclass
class Report:
    errors: list[Finding] = field(default_factory=list)
    warnings: list[Finding] = field(default_factory=list)

    def error(self, path: str, message: str, line: int | None = None) -> None:
        self.errors.append(Finding(path, message, line))

    def warn(self, path: str, message: str, line: int | None = None) -> None:
        self.warnings.append(Finding(path, message, line))


# ── 배포 단위 ───────────────────────────────────────────────────

@dataclass(frozen=True)
class Unit:
    """manifest에 등록되어야 하는 배포 단위 하나 (스킬 디렉터리, 에이전트 파일, 훅 모듈)."""

    kind: str  # skill / agent / hook
    side: str  # claude / codex
    key: str  # 양쪽을 짝짓는 이름. 아카이브는 "<name>/<vN>"
    path: str  # 레포 루트 기준 경로. 디렉터리는 끝에 "/"를 붙이지 않는다
    expected_name: str  # frontmatter/TOML의 name 값

    @property
    def label(self) -> str:
        return f"{self.side} {self.kind} '{self.key}'"


def _rel(root: Path, path: Path) -> str:
    return path.relative_to(root).as_posix()


def _dirs(path: Path) -> list[Path]:
    if not path.is_dir():
        return []
    return sorted(p for p in path.iterdir() if p.is_dir())


def _files(path: Path, suffix: str) -> list[Path]:
    if not path.is_dir():
        return []
    return sorted(p for p in path.iterdir() if p.is_file() and p.suffix == suffix and p.name != "README.md")


def collect_units(root: Path) -> list[Unit]:
    units: list[Unit] = []

    for side, base in (("claude", ".claude/skills"), ("codex", ".agents/skills")):
        for d in _dirs(root / base):
            if d.name == "_archive":
                for name_dir in _dirs(d):
                    for version_dir in _dirs(name_dir):
                        units.append(Unit("skill", side, f"{name_dir.name}/{version_dir.name}",
                                          _rel(root, version_dir), name_dir.name))
            else:
                units.append(Unit("skill", side, d.name, _rel(root, d), d.name))

    for side, base, suffix in (("claude", ".claude/agents", ".md"), ("codex", ".codex/agents", ".toml")):
        for f in _files(root / base, suffix):
            units.append(Unit("agent", side, f.stem, _rel(root, f), f.stem))
        for name_dir in _dirs(root / base / "_archive"):
            for f in _files(name_dir, suffix):
                units.append(Unit("agent", side, f"{name_dir.name}/{f.stem}", _rel(root, f), name_dir.name))

    for side, base in (("claude", ".claude/hooks"), ("codex", ".codex/hooks")):
        dispatcher = root / base / "dispatcher"
        if dispatcher.is_dir():
            units.append(Unit("hook", side, "dispatcher", _rel(root, dispatcher), "dispatcher"))
        for d in _dirs(root / base / "modules"):
            units.append(Unit("hook", side, d.name, _rel(root, d), d.name))

    return units


# ── manifest ─────────────────────────────────────────────────

def _is_safe_relative(path: str) -> bool:
    pure = PurePosixPath(path)
    return not pure.is_absolute() and ".." not in pure.parts and path not in ("", ".", "./")


def _norm(path: str) -> str:
    return path.rstrip("/")


def _is_under(path: str, parent: str) -> bool:
    """path가 parent와 같거나 parent 디렉터리 아래에 있는지."""
    path, parent = _norm(path), _norm(parent)
    return path == parent or path.startswith(parent + "/")


def _id_lines(manifest_text: str) -> dict[str, int]:
    lines: dict[str, int] = {}
    for number, line in enumerate(manifest_text.splitlines(), start=1):
        match = re.match(r"^\s*-\s*id:\s*['\"]?([^'\"#\s]+)", line)
        if match:
            lines.setdefault(match.group(1), number)
    return lines


def _key_line(manifest_text: str, key: str) -> int | None:
    for number, line in enumerate(manifest_text.splitlines(), start=1):
        if line.startswith(f"{key}:"):
            return number
    return None


def check_manifest(root: Path, report: Report) -> list[dict]:
    """manifest를 검사하고 형식이 올바른 항목만 돌려준다."""
    path = root / MANIFEST_NAME
    if not path.is_file():
        report.error(MANIFEST_NAME, "파일이 없습니다")
        return []

    text = path.read_text(encoding="utf-8")
    try:
        manifest = yaml.safe_load(text)
    except yaml.YAMLError as exc:
        mark = getattr(exc, "problem_mark", None)
        report.error(MANIFEST_NAME, f"YAML 파싱 실패: {exc}", mark.line + 1 if mark else None)
        return []

    if not isinstance(manifest, dict) or not isinstance(manifest.get("items"), list):
        report.error(MANIFEST_NAME, "최상위에 `items` 목록이 있어야 합니다")
        return []

    id_lines = _id_lines(text)
    items: list[dict] = []
    seen: set[str] = set()

    for index, item in enumerate(manifest["items"]):
        if not isinstance(item, dict):
            report.error(MANIFEST_NAME, f"items[{index}]가 매핑이 아닙니다")
            continue
        item_id = item.get("id")
        if not isinstance(item_id, str) or not item_id:
            report.error(MANIFEST_NAME, f"items[{index}]에 `id`가 없습니다")
            continue
        line = id_lines.get(item_id)
        where = f"`{item_id}`"

        if item_id in seen:
            report.error(MANIFEST_NAME, f"{where}: id가 중복됩니다", line)
            continue
        seen.add(item_id)

        valid = True
        for key in ("src", "dest"):
            value = item.get(key)
            if not isinstance(value, str) or not value:
                report.error(MANIFEST_NAME, f"{where}: `{key}`가 비어 있거나 문자열이 아닙니다", line)
                valid = False
            elif not _is_safe_relative(value):
                report.error(MANIFEST_NAME, f"{where}: `{key}`는 레포 루트 기준 상대 경로여야 합니다 ({value})", line)
                valid = False
        groups = item.get("groups")
        if not isinstance(groups, list) or not all(isinstance(g, str) for g in groups):
            # 동기화 스크립트가 item["groups"]를 바로 읽으므로 빈 목록이라도 있어야 한다
            report.error(MANIFEST_NAME, f"{where}: `groups`는 문자열 목록이어야 합니다 (opt-in 전용은 `[]`)", line)
            valid = False
        if not valid:
            continue

        src, dest = item["src"], item["dest"]
        src_path = root / src
        if src.endswith("/"):
            if not src_path.is_dir():
                report.error(MANIFEST_NAME, f"{where}: src 디렉터리가 없습니다 ({src})", line)
            if not dest.endswith("/"):
                report.error(MANIFEST_NAME, f"{where}: src가 디렉터리면 dest도 `/`로 끝나야 합니다 ({dest})", line)
        else:
            if src_path.is_dir():
                report.error(MANIFEST_NAME, f"{where}: src가 디렉터리입니다. 끝에 `/`를 붙이세요 ({src})", line)
            elif not src_path.is_file():
                report.error(MANIFEST_NAME, f"{where}: src 파일이 없습니다 ({src})", line)
            if dest.endswith("/"):
                report.error(MANIFEST_NAME, f"{where}: src가 파일이면 dest는 `/`로 끝나면 안 됩니다 ({dest})", line)

        if "@" in item_id and not VERSIONED_ID.match(item_id):
            report.error(MANIFEST_NAME, f"{where}: 버전 고정 id는 `<id>@v<숫자>` 형식이어야 합니다", line)

        # 동기화 항목 화면이 스킬·에이전트를 domain으로 묶으므로 누락되면 분류에서 빠진다. 버전 고정 항목은 기준 항목을 따른다
        if (
            DOMAIN_REQUIRED_ID.match(item_id)
            and not VERSIONED_ID.match(item_id)
            and item.get("domain") not in DOMAINS
        ):
            report.error(
                MANIFEST_NAME,
                f"{where}: 스킬·에이전트 항목의 `domain`은 {' / '.join(sorted(DOMAINS))} 중 하나여야 합니다",
                line,
            )

        items.append(item)

    # 같은 dest에 두 항목을 쓰면 나중 항목이 조용히 덮어쓴다. @vN 항목은 기준 항목과 dest가 같아야 하므로 아래에서 따로 본다
    dest_owners: dict[str, str] = {}
    for item in items:
        if VERSIONED_ID.match(item["id"]):
            continue
        dest = _norm(item["dest"])
        owner = dest_owners.setdefault(dest, item["id"])
        if owner != item["id"]:
            report.error(
                MANIFEST_NAME,
                f"`{item['id']}`: dest({item['dest']})가 `{owner}`와 겹칩니다",
                id_lines.get(item["id"]),
            )

    by_id = {item["id"]: item for item in items}
    for item in items:
        match = VERSIONED_ID.match(item["id"])
        if not match:
            continue
        line = id_lines.get(item["id"])
        where = f"`{item['id']}`"
        base = by_id.get(match.group("base"))
        if base is None:
            report.error(MANIFEST_NAME, f"{where}: 기준 항목 `{match.group('base')}`가 manifest에 없습니다", line)
        elif _norm(base["dest"]) != _norm(item["dest"]):
            report.error(MANIFEST_NAME, f"{where}: dest가 기준 항목과 다릅니다 ({item['dest']} ≠ {base['dest']})", line)
        if item["groups"]:
            report.error(MANIFEST_NAME, f"{where}: 버전 고정 항목은 `groups: []`로 opt-in 전용이어야 합니다", line)

    used_groups = {g for item in items for g in item["groups"]}
    defaults = manifest.get("defaults")
    defaults_line = _key_line(text, "defaults")
    if defaults is None:
        # 동기화 스크립트는 defaults가 없으면 빈 목록으로 읽어, .harness/sync.yml이 없는 레포에 아무것도 배포하지 않는다
        report.error(MANIFEST_NAME, "`defaults`가 없습니다. .harness/sync.yml이 없는 레포에 적용할 그룹을 지정하세요", defaults_line)
    elif not isinstance(defaults, list) or not all(isinstance(g, str) for g in defaults):
        report.error(MANIFEST_NAME, "`defaults`는 문자열 목록이어야 합니다", defaults_line)
    elif not defaults:
        report.error(MANIFEST_NAME, "`defaults`가 비어 있습니다. .harness/sync.yml이 없는 레포에 아무것도 배포되지 않습니다", defaults_line)
    else:
        for group in defaults:
            if group not in used_groups:
                report.error(MANIFEST_NAME, f"defaults의 그룹 `{group}`을 쓰는 항목이 없습니다", defaults_line)

    deletions = manifest.get("deletions")
    deletions_line = _key_line(text, "deletions")
    if deletions is not None:
        if not isinstance(deletions, list):
            report.error(MANIFEST_NAME, "`deletions`는 목록이어야 합니다", deletions_line)
        else:
            for index, entry in enumerate(deletions):
                dest = entry.get("dest") if isinstance(entry, dict) else None
                if not isinstance(dest, str) or not dest:
                    report.error(MANIFEST_NAME, f"deletions[{index}]에 `dest`가 없습니다", deletions_line)
                    continue
                if not _is_safe_relative(dest):
                    report.error(MANIFEST_NAME, f"deletions[{index}]: dest는 상대 경로여야 합니다 ({dest})", deletions_line)
                    continue
                for item in items:
                    if _is_under(item["dest"], dest):
                        report.error(
                            MANIFEST_NAME,
                            f"deletions의 `{dest}`가 현재 배포 항목 `{item['id']}`의 dest({item['dest']})를 지웁니다",
                            deletions_line,
                        )

    if "claude/settings" in by_id and not (root / SETTINGS_BASE).is_file():
        report.error(
            MANIFEST_NAME,
            f"`claude/settings`는 훅이 없을 때 {SETTINGS_BASE}를 배포하는데 파일이 없습니다",
            id_lines.get("claude/settings"),
        )

    return items


def check_registration(units: list[Unit], items: list[dict], report: Report) -> None:
    sources = [_norm(item["src"]) for item in items]
    for unit in units:
        if not any(_is_under(unit.path, src) for src in sources):
            report.error(unit.path, f"{unit.label}가 {MANIFEST_NAME}에 등록되지 않았습니다")


# ── 문법 ─────────────────────────────────────────────────────

def _read_frontmatter(path: Path, rel: str, report: Report) -> dict | None:
    text = path.read_text(encoding="utf-8")
    match = FRONTMATTER.match(text)
    if not match:
        report.error(rel, "파일 맨 앞에 `---`로 감싼 YAML frontmatter가 없습니다", 1)
        return None
    try:
        data = yaml.safe_load(match.group(1))
    except yaml.YAMLError as exc:
        mark = getattr(exc, "problem_mark", None)
        report.error(rel, f"frontmatter YAML 파싱 실패: {exc}", mark.line + 2 if mark else 1)
        return None
    if not isinstance(data, dict):
        report.error(rel, "frontmatter가 매핑이 아닙니다", 1)
        return None
    return data


def _toml_error_line(exc: ValueError) -> int | None:
    """TOMLDecodeError의 줄 번호. `lineno` 속성은 Python 3.14부터 있으므로 그 전에는 메시지에서 읽는다."""
    line = getattr(exc, "lineno", None)
    if isinstance(line, int):
        return line
    match = TOML_ERROR_LINE.search(str(exc))
    return int(match.group(1)) if match else None


def _check_name_and_description(data: dict, unit: Unit, rel: str, report: Report) -> None:
    for key in ("name", "description"):
        value = data.get(key)
        if not isinstance(value, str) or not value.strip():
            report.error(rel, f"`{key}`가 비어 있거나 문자열이 아닙니다")
    name = data.get("name")
    if isinstance(name, str) and name != unit.expected_name:
        report.error(rel, f"`name`({name})이 경로의 이름({unit.expected_name})과 다릅니다")


def check_syntax(root: Path, units: list[Unit], report: Report) -> None:
    for unit in units:
        if unit.kind == "skill":
            rel = f"{unit.path}/SKILL.md"
            path = root / rel
            if not path.is_file():
                report.error(unit.path, f"{unit.label}에 SKILL.md가 없습니다")
                continue
            data = _read_frontmatter(path, rel, report)
            if data is not None:
                _check_name_and_description(data, unit, rel, report)

        elif unit.kind == "agent" and unit.side == "claude":
            data = _read_frontmatter(root / unit.path, unit.path, report)
            if data is not None:
                _check_name_and_description(data, unit, unit.path, report)

        elif unit.kind == "agent" and unit.side == "codex":
            raw = (root / unit.path).read_text(encoding="utf-8")
            try:
                data = tomllib.loads(raw)
            except tomllib.TOMLDecodeError as exc:
                report.error(unit.path, f"TOML 파싱 실패: {exc}", _toml_error_line(exc))
                continue
            for key in CODEX_AGENT_REQUIRED:
                value = data.get(key)
                if not isinstance(value, str) or not value.strip():
                    report.error(unit.path, f"`{key}`가 비어 있거나 문자열이 아닙니다")
            name = data.get("name")
            if isinstance(name, str) and name != unit.expected_name:
                report.error(unit.path, f"`name`({name})이 경로의 이름({unit.expected_name})과 다릅니다")
            if "developer_instructions" in data and not TOML_LITERAL_INSTRUCTIONS.search(raw):
                report.error(unit.path, "`developer_instructions`는 literal string(`'''…'''`)으로 작성해야 합니다")
            if "model" in data:
                report.error(unit.path, "`model`은 생략해 부모 세션 설정을 상속합니다. 작업 무게는 `model_reasoning_effort`로 조절하세요")
            effort = data.get("model_reasoning_effort")
            if effort is not None and effort not in CODEX_REASONING_EFFORTS:
                report.error(unit.path, f"`model_reasoning_effort`는 {sorted(CODEX_REASONING_EFFORTS)} 중 하나여야 합니다 ({effort})")
            sandbox = data.get("sandbox_mode")
            if sandbox is not None and sandbox not in CODEX_SANDBOX_MODES:
                report.error(unit.path, f"`sandbox_mode`는 {sorted(CODEX_SANDBOX_MODES)} 중 하나여야 합니다 ({sandbox})")

    # 에이전트 TOML은 위에서 필드까지 검사했으므로 `.codex/config.toml` 같은 나머지 설정 파일만 파싱한다
    agent_files = {u.path for u in units if u.kind == "agent" and u.side == "codex"}
    for base in CONFIG_ROOTS:
        for path in sorted((root / base).rglob("*.json")):
            rel = _rel(root, path)
            try:
                json.loads(path.read_text(encoding="utf-8"))
            except json.JSONDecodeError as exc:
                report.error(rel, f"JSON 파싱 실패: {exc.msg}", exc.lineno)
        for path in sorted((root / base).rglob("*.toml")):
            rel = _rel(root, path)
            if rel in agent_files:
                continue
            try:
                tomllib.loads(path.read_text(encoding="utf-8"))
            except tomllib.TOMLDecodeError as exc:
                report.error(rel, f"TOML 파싱 실패: {exc}", _toml_error_line(exc))


# ── Claude/Codex 대응 (경고) ─────────────────────────────────

def _hook_events(root: Path, unit: Unit) -> set[str]:
    """훅 모듈의 이벤트 파일을 Codex 이름으로 정규화해 돌려준다."""
    names = {p.name for p in (root / unit.path).iterdir() if p.is_file()}
    if unit.side == "claude":
        return {HOOK_EVENT_FILES.get(n, n) for n in names}
    return names


def check_parity(root: Path, units: list[Unit], report: Report) -> None:
    index = {(u.kind, u.side, u.key): u for u in units}
    for unit in units:
        other_side = "codex" if unit.side == "claude" else "claude"
        counterpart = index.get((unit.kind, other_side, unit.key))
        if counterpart is None:
            report.warn(unit.path, f"{unit.label}에 대응하는 {other_side} 항목이 없습니다")
            continue
        if unit.kind == "hook" and unit.side == "claude":
            mine, theirs = _hook_events(root, unit), _hook_events(root, counterpart)
            if mine != theirs:
                report.warn(
                    unit.path,
                    f"{unit.label}의 이벤트 파일이 codex와 다릅니다 "
                    f"(claude만: {sorted(mine - theirs)}, codex만: {sorted(theirs - mine)})",
                )


def _git(root: Path, *args: str) -> str:
    result = subprocess.run(["git", *args], cwd=root, capture_output=True, text=True)
    if result.returncode != 0:
        raise RuntimeError(f"git {' '.join(args)} 실패: {result.stderr.strip()}")
    return result.stdout


def changed_files(root: Path, base: str) -> list[str]:
    """base와 갈라진 지점 이후 바뀐 파일. 커밋 전에도 쓸 수 있도록 작업 트리와 추적되지 않은 파일을 포함한다.

    하네스가 저장소의 하위 폴더에 있을 수 있으므로 --relative로 root 아래 변경만 root 기준 경로로 받는다.
    """
    merge_base = _git(root, "merge-base", base, "HEAD").strip()
    tracked = _git(root, "diff", "--name-only", "--relative", merge_base).splitlines()
    untracked = _git(root, "ls-files", "--others", "--exclude-standard").splitlines()
    return sorted({line for line in tracked + untracked if line})


def check_one_sided_changes(units: list[Unit], changed: list[str], report: Report) -> None:
    touched = {
        (u.kind, u.side, u.key)
        for u in units
        for path in changed
        if _is_under(path, u.path)
    }
    index = {(u.kind, u.side, u.key): u for u in units}
    for kind, side, key in sorted(touched):
        other_side = "codex" if side == "claude" else "claude"
        counterpart = index.get((kind, other_side, key))
        if counterpart is not None and (kind, other_side, key) not in touched:
            unit = index[(kind, side, key)]
            report.warn(unit.path, f"{unit.label}만 수정되고 {other_side} 쪽({counterpart.path})은 수정되지 않았습니다")


# ── 실행 ─────────────────────────────────────────────────────

def validate(root: Path, changed: list[str] | None = None) -> Report:
    report = Report()
    items = check_manifest(root, report)
    units = collect_units(root)
    if items:
        check_registration(units, items, report)
    check_syntax(root, units, report)
    check_parity(root, units, report)
    if changed is not None:
        check_one_sided_changes(units, changed, report)
    return report


def _print(findings: list[Finding], level: str, github: bool) -> None:
    for f in findings:
        if github:
            location = f"file={f.path}" + (f",line={f.line}" if f.line else "")
            # workflow command는 줄 단위라 개행이 있으면 annotation이 잘린다
            message = f.message.replace("%", "%25").replace("\r", "").replace("\n", "%0A")
            print(f"::{level} {location}::{message}")
        else:
            location = f.path + (f":{f.line}" if f.line else "")
            print(f"  [{level}] {location}: {f.message}")


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--base", help="이 커밋과 갈라진 뒤 Claude/Codex 한쪽만 수정된 항목을 경고합니다. 커밋하지 않은 변경도 포함합니다 (예: origin/develop)")
    parser.add_argument("--root", type=Path, default=ROOT, help=argparse.SUPPRESS)
    args = parser.parse_args(argv)

    changed = changed_files(args.root, args.base) if args.base else None
    report = validate(args.root, changed)

    github = os.environ.get("GITHUB_ACTIONS") == "true"
    _print(report.errors, "error", github)
    _print(report.warnings, "warning", github)
    print(f"에러 {len(report.errors)}개, 경고 {len(report.warnings)}개")
    return 1 if report.errors else 0


if __name__ == "__main__":
    sys.exit(main())
