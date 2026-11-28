---
title: Codex 컨벤션
description: Codex 스킬, 에이전트, 훅 작성 규칙
order: 70
---

# Codex Conventions

`.codex/` 및 `.agents/` 디렉토리 하위 파일 작성 규칙입니다.

## 프로젝트 지침

프로젝트 공통 지침은 `AGENTS.md`에서 관리합니다. 하네스의 스킬·에이전트는 작업 경로에 적용되는 파일을 참고하고, 파일이 없으면 존재하는 규칙 문서와 코드·도구 설정을 확인합니다. 런타임이 `AGENTS.override.md`나 설정된 대체 파일명을 선택했다면 그 결과를 존중합니다. 적용 범위와 충돌 처리 기준은 [전역 컨벤션](./global.md#프로젝트-지침-agentsmd)을 참고하세요.

## 디렉토리 구조

```
.codex/
├── config.toml     # Codex 전역 설정
├── hooks.json      # 훅 이벤트 매핑 (dispatcher 등록)
├── agents/         # .claude/agents/와 동일한 서브에이전트 (Codex TOML 포맷)
│   └── agent-name.toml
└── hooks/
    ├── dispatcher/
    │   ├── session-start.sh  ← modules/*/session-start.sh 자동 스캔·실행
    │   ├── pre-tool-use.sh   ← modules/*/pre-tool-use.sh 자동 스캔·실행
    │   └── post-tool-use.sh
    └── modules/
        ├── ponytail/         ← session-start.sh
        ├── logging/          ← pre-tool-use.sh
        ├── command-guard/    ← pre-tool-use.sh
        ├── ktlint/           ← post-tool-use.sh
        └── gradle-test/      ← post-tool-use.sh

.agents/
└── skills/         # .claude/skills/와 동일한 스킬 (Codex 호환 포맷)
    └── skill-name/
        └── SKILL.md
```

## config.toml

모델, 승인 정책, 훅 활성화 등 Codex 전역 동작을 설정합니다.

```toml
model = "..."
model_reasoning_effort = "high"
web_search = "live"

[approval]
policy = "on-request"

[shell]
login_shell_allowed = true

[features]
hooks = true   # 훅 모듈 사용 시 반드시 true
```

- 프로젝트 공통 설정만 포함
- API 키·시크릿 절대 포함 금지

## hooks.json

dispatcher를 Codex 훅으로 등록하는 파일입니다. `codex/hooks-json` 항목으로 opt-in 배포됩니다.  
`overrides`에서 `codex/hooks/dispatcher: true`도 설정하면, dispatcher 파일이 타깃 레포의 `.codex/hooks/` 바로 아래에 배포됩니다.

```json
{
  "hooks": {
    "SessionStart": [{ "command": ".codex/hooks/session-start.sh" }],
    "PreToolUse": [{ "command": ".codex/hooks/pre-tool-use.sh" }],
    "PostToolUse": [{ "command": ".codex/hooks/post-tool-use.sh" }]
  }
}
```

## `.codex/agents/` 서브에이전트

`.claude/agents/`와 동일한 서브에이전트를 Codex TOML 포맷으로 제공합니다.  
Codex는 명시적 위임 요청 또는 트리거 문구를 감지하면 이 에이전트를 스폰합니다. 에이전트 설명은 [에이전트 레퍼런스](../reference/agents.md)를 참고하세요.

각 `.toml` 파일의 필수 필드는 `name` / `description` / `developer_instructions`이며, 선택 필드로 `model_reasoning_effort`(`low`/`medium`/`high`)와 `sandbox_mode`(`read-only`/`workspace-write`)를 사용합니다.

```toml
name = "agent-name"
description = '''트리거 문구를 포함한 설명. 예: '컨벤션 검사해줘' / 'agent-name 실행해' '''
model_reasoning_effort = "medium"
sandbox_mode = "read-only"
developer_instructions = '''
에이전트 본문 (Role → Context → Steps → Output → Constraints).
'''
```

작성 규칙:

- `developer_instructions`는 **literal string**(`'''…'''`)으로 작성한다 — 본문에 포함된 grep 정규식의 백슬래시(`\.kt`, `\s` 등)가 basic string 이스케이프와 충돌하기 때문
- `model`은 생략해 `config.toml`의 전역 모델을 상속한다 (잘못된 모델 ID 위험 회피). 작업 무게는 `model_reasoning_effort`로 차등
- 읽기 전용 에이전트는 `sandbox_mode = "read-only"`, 파일을 편집하는 에이전트는 `"workspace-write"`
- `.claude/`와 `.codex/`는 독립 시스템이다 — 본문은 같아도 두 파일을 각각 유지·등록해야 한다

## 훅 모듈 (`hooks/modules/`)

dispatcher가 런타임에 `modules/` 디렉토리를 스캔하여 모듈을 자동 실행합니다.

| 모듈 | 진입 파일 | 실행 시점 |
|------|----------|----------|
| `ponytail` | `session-start.sh` | 세션 시작 |
| `logging` | `pre-tool-use.sh` | 도구 실행 전 |
| `command-guard` | `pre-tool-use.sh` | 도구 실행 전 |
| `ktlint` | `post-tool-use.sh` | 도구 실행 후 |
| `gradle-test` | `post-tool-use.sh` | 도구 실행 후 |

모듈 작성 규칙 (`exit 0` / `exit 2` 등)은 [Claude 컨벤션 — 훅](./claude.md#훅-hooks)과 동일합니다.
`session-start.sh` 모듈은 컨텍스트에 넣을 내용을 일반 텍스트로 출력합니다. dispatcher가 모듈 출력을 모아 Codex가 읽는 `additionalContext` JSON으로 감쌉니다.
모든 훅 항목은 어느 그룹에도 기본 포함되지 않으며 `overrides`에 `true`로 명시해야 합니다.

## `.agents/skills/` 스킬

`.claude/skills/`와 동일한 스킬을 Codex 호환 포맷으로 제공합니다.  
새 스킬 추가 시 두 디렉토리 모두 업데이트해야 합니다.

SKILL.md 작성 규칙은 [Claude 컨벤션 — 스킬](./claude.md#스킬-skills)을 따릅니다.
