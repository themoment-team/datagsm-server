---
title: Claude 컨벤션
description: Claude 스킬, 에이전트, 훅 작성 규칙
order: 60
---

# Claude Conventions

`.claude/` 디렉토리 하위 파일 작성 규칙입니다.

## 프로젝트 지침

공통 지침은 저장소 루트의 `AGENTS.md`에 작성하고, 경로별 지침은 해당 디렉터리의 `AGENTS.md`에 둡니다. 하네스는 이 파일을 동기화하지 않습니다. 파일이 없는 저장소의 처리 기준은 [전역 컨벤션](./global.md#프로젝트-지침-agentsmd)을 참고하세요.

Claude Code의 직접 로딩 지원을 위해 v2.1.281 이상을 사용하고, 내장 `agents-md` 플러그인이 활성화되어 있는지 확인하세요. 기본 **Project instructions** 값인 `claude-md-or-agents-md`에서는 작업 디렉터리 또는 그 상위 디렉터리에 `CLAUDE.md`, `.claude/CLAUDE.md`, `CLAUDE.local.md` 중 하나라도 있으면 `AGENTS.md`를 직접 로딩하지 않습니다. 사용자 전역 `~/.claude/CLAUDE.md`, 조직 관리 지침과 `.claude/rules/`는 이 조건에 포함되지 않습니다.

기존 프로젝트 지침과 공존해야 한다면 `/config`에서 **Project instructions**를 `claude-md-and-agents-md`로 설정하거나, 기존 `CLAUDE.md`에서 `@AGENTS.md`로 공통 지침을 가져오세요. import 경로는 해당 파일 기준이므로 `.claude/CLAUDE.md`에서 루트 파일을 가져올 때는 `@../AGENTS.md`를 사용합니다. 공통 규칙은 `AGENTS.md` 한 곳에서 관리하고 `/memory`에서 실제 로딩을 확인하세요. 세부 조건은 [Claude Code 공식 문서](https://code.claude.com/docs/en/memory#agentsmd)를 참고하세요.

## 디렉토리 구조

```
.claude/
├── agents/       # 서브에이전트 정의
├── hooks/        # dispatcher + 모듈 기반 훅 (opt-in 동기화)
├── rules/        # 각 프로젝트가 직접 관리 (동기화 제외)
├── settings.json # 훅 포함 시 자동 배포, 미포함 시 settings-base.json 배포
└── skills/       # 스킬
```

---

## 스킬 (`skills/`)

### 디렉토리 구조

```
skills/
└── skill-name/
    ├── SKILL.md          # 필수 — 스킬 진입점
    ├── references/       # 선택 — 참조 문서
    └── scripts/          # 선택 — 실행 스크립트
```

### SKILL.md 프런트매터

```yaml
---
name: skill-name                          # 필수 — 호출 이름 (/skill-name)
description: "한 줄 설명 (영어)"           # 필수
argument-hint: "[argument description]"   # 선택 — 인자가 있을 때
allowed-tools: Bash, Edit, Read, ...      # 선택 — 허용 도구 명시
disable-model-invocation: true            # 선택 — 서브모델 호출 비활성화
---
```

### description 작성 규칙

- **영어**로 작성
- 무엇을 하는지(What) + 어떻게(How) + 언제 쓰는지(When) 포함
- 트리거 조건, 제외 조건 명시

**좋은 예**
```
description: "Run structured security checklist over changed files — hardcoded secrets,
SQL injection, JWT validation, API key masking. Run before merging auth or API-related changes."
```

**나쁜 예**
```
description: "Security check"   # 너무 짧음
description: "보안 검사합니다"   # 한국어 금지
```

### allowed-tools 지침

- 명시하지 않으면 모든 도구 허용 → 위험할 수 있음
- Bash를 허용할 때는 가능한 좁게: `Bash(git *:*)`, `Bash(./gradlew *:*)`
- Read-only 스킬은 `Bash, Glob, Grep, Read`만 허용

---

## 에이전트 (`agents/`)

### 파일 구조

```yaml
---
name: agent-name
description: "..."
tools: Bash, Glob, Grep, Read, Edit, ...
model: sonnet | haiku | opus
---

[에이전트 본문 — 선택]
```

### description 작성 규칙

description이 에이전트의 핵심 — Claude가 이걸 보고 자동 위임 여부를 결정합니다.

필수 포함 항목:

1. **무엇을 하는지** — 동작 설명
2. **트리거 문구** — 한국어 예시 포함 (`Trigger phrases: '...'`)
3. **DO NOT trigger** — 혼동될 수 있는 케이스 명시

**구조 예시**
```
"동작 설명. 트리거 문구 예시: '...',  '...'. DO NOT trigger when ..."
```

### model 선택 기준

| model | 사용 시점 |
|-------|----------|
| `haiku` | 단순 정보 수집, 웹 검색, 빠른 검사 |
| `sonnet` | 코드 분석·수정, 문서 작성 (기본값) |
| `opus` | 복잡한 추론, 아키텍처 설계 |

---

## 훅 (`hooks/`)

### 디렉토리 구조

dispatcher가 런타임에 `modules/` 디렉토리를 스캔하여 모듈을 자동 실행합니다.

```
.claude/hooks/
├── dispatcher/
│   ├── sessionStart.sh  ← modules/*/sessionStart.sh 자동 스캔·실행
│   ├── preToolUse.sh    ← modules/*/preToolUse.sh 자동 스캔·실행
│   └── postToolUse.sh
└── modules/
    ├── ponytail/        ← sessionStart.sh
    ├── logging/         ← preToolUse.sh
    ├── command-guard/   ← preToolUse.sh
    ├── ktlint/          ← postToolUse.sh
    └── gradle-test/     ← postToolUse.sh
```

### dispatcher 진입점

`claude/hooks/dispatcher` 항목 배포 시 dispatcher 파일이 타깃 레포의 `.claude/hooks/` 바로 아래에 위치합니다.

| 파일 (타깃 레포 기준) | 실행 시점 |
|----------------------|----------|
| `.claude/hooks/sessionStart.sh` | 세션 시작, `/clear`, 컨텍스트 압축 직후 |
| `.claude/hooks/preToolUse.sh` | 모든 도구 실행 전 |
| `.claude/hooks/postToolUse.sh` | Edit·Write 실행 후 |

### 모듈 작성 규칙

- `#!/bin/bash` 또는 `#!/bin/sh` 헤더 필수
- exit code: `0` = 정상 통과, `2` = 도구 실행 차단 (preToolUse), 그 외 = 오류
- `sessionStart.sh` 모듈이 stdout에 쓴 내용은 세션 컨텍스트에 추가됨. 넣을 내용이 없으면 아무것도 출력하지 않고 `exit 0`
- 무거운 작업 금지 — 훅은 빠르게 끝나야 함
- 새 모듈 추가 시 `sync-manifest.yml`에 `groups: []`로 등록 (opt-in 전용)

---

## 규칙 (`rules/`) — 각 프로젝트 직접 관리

동기화 대상이 아닙니다. 각 프로젝트 저장소에서 직접 관리하세요.

파일당 하나의 주제만 다룹니다:

```
rules/
├── kotlin-style.md      # Kotlin 코딩 스타일
├── dto-annotations.md   # DTO 어노테이션 규칙
├── logging.md           # 로깅 규칙
├── exception.md         # 예외 처리 규칙
└── api-conventions.md   # API 설계 규칙
```
