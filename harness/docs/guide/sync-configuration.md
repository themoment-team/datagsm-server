---
title: 동기화 설정
description: 프로젝트별 동기화 항목과 PR 설정
order: 30
---

# Per-Repo Config

타깃 레포에서 어떤 파일을 받을지 직접 제어하는 방법입니다.

## 기본 동작

`.harness/sync.yml`이 없으면 기본 그룹(`claude`, `codex`, `gemini`)이 모두 동기화됩니다.  
훅 관련 항목은 기본 그룹에 포함되지 않으며 **`overrides`에 `true`로 명시**해야 합니다.

기본 그룹으로 대체되는 경우는 파일이 없다고 확인된 경우(HTTP 404)뿐이며, 빈 파일도 기본값이 적용됩니다. 권한 부족·rate limit·서버 오류로 파일을 조회하지 못하거나 내용을 YAML 매핑으로 해석할 수 없으면 해당 레포는 이번 회차에서 건너뛰고 동기화 PR을 만들지 않습니다. 건너뛴 레포는 워크플로우 Job Summary에 **설정 조회 실패**로 집계되며, 다음 실행 때 다시 시도합니다.

## 프로젝트 지침은 각 저장소에서 관리

`AGENTS.md`, `.claude/rules/**`, `.gemini/styleguide.md`, `.github/copilot-instructions.md`는 배포 항목이 아닙니다. 각 타깃 레포가 직접 관리하며, 하네스는 이를 생성·덮어쓰기·삭제하지 않습니다.

`AGENTS.md`가 없어도 동기화와 스킬·에이전트 사용을 계속할 수 있습니다. 규칙 문서의 적용 범위·충돌·부재 시 처리 기준은 [전역 컨벤션](./conventions/global.md#프로젝트-지침-agentsmd)을 참고하세요.

## 동기화 주기 및 실행 조건

자동 동기화 워크플로우는 다음 시점에 실행됩니다:

1. **기본 브랜치 반영 시 (`push`):**
   - datagsm-server의 `master` 브랜치에 `harness/` 변경이 반영되면 즉시 각 타깃 레포에 동기화 PR이 생성됩니다.
2. **매일 정기 실행 (`schedule`):**
   - 매일 00:00 UTC (한국 시간 기준 **오전 09:00**)에 자동으로 실행되어 전체 타깃 레포의 상태를 동기화합니다.
3. **수동 실행 (`workflow_dispatch`):**
   - datagsm-server의 GitHub Actions에서 `Sync AI Harness files` 워크플로에 `target_repo`를 넣어 특정 설치 레포만 동기화할 수 있습니다.

## 설정 파일 위치

```
your-repo/
└── .harness/
    └── sync.yml
```

> `.harness/sync.yml`은 **레포의 기본 브랜치**에서 읽습니다. `base_branch: develop`을 사용하더라도 이 설정 파일은 기본 브랜치에 있어야 합니다.

## 설정 형식

```yaml
enabled: true   # 이 레포의 동기화 사용 여부 (기본값: true)

groups:
  - claude    # Claude Code 스킬·에이전트
  - codex     # Codex/Agents CLI 스킬·설정
  - gemini    # Gemini CLI 설정

# false: 그룹 내 항목 제외 | true: opt-in 항목 추가 | "v1": 버전 고정
overrides:
  claude/skills/kotest-guide: false  # 이 스킬은 받지 않음
  claude/hooks/dispatcher: true      # Claude 훅 활성화 (opt-in)
  claude/settings: true
  claude/hooks/ktlint: true
  codex/hooks/dispatcher: true       # Codex 훅 활성화 (Claude와 별개 — 공유되지 않음)
  codex/hooks-json: true
  codex/hooks/ktlint: true

# PR 설정 (생략하면 기본값 적용)
branch_prefix: harness-sync/
base_branch: main              # sync 브랜치를 분기하고 PR을 보낼 대상 브랜치
language: ko                   # sync PR 제목·본문 언어 (기본값: en)
pr_label: false                # sync PR 라벨 부착 여부 (기본값: true)
```

## 사용 가능한 그룹

| 그룹        | 동기화되는 경로                                |
|-----------|-----------------------------------------|
| `claude`  | `.claude/skills/`, `.claude/agents/`    |
| `codex`   | `.agents/skills/`, `.codex/agents/`, `.codex/config.toml` |
| `gemini`  | `.gemini/settings.json`                 |

## 항목 ID 목록

각 항목의 설명은 [스킬 레퍼런스](./reference/skills.md), [에이전트 레퍼런스](./reference/agents.md), [훅 레퍼런스](./reference/hooks.md)를 참고하세요.

### claude 그룹

| ID | 배포 경로 |
|----|----------|
| `claude/skills/api-design` | `.claude/skills/api-design/` |
| `claude/skills/database-schema` | `.claude/skills/database-schema/` |
| `claude/skills/docker` | `.claude/skills/docker/` |
| `claude/skills/git-commit` | `.claude/skills/git-commit/` |
| `claude/skills/humanizer` | `.claude/skills/humanizer/` |
| `claude/skills/ponytail` | `.claude/skills/ponytail/` |
| `claude/skills/ponytail-audit` | `.claude/skills/ponytail-audit/` |
| `claude/skills/kotest-guide` | `.claude/skills/kotest-guide/` |
| `claude/skills/kotlin-spring-arch` | `.claude/skills/kotlin-spring-arch/` |
| `claude/skills/migration-guide` | `.claude/skills/migration-guide/` |
| `claude/skills/planning` | `.claude/skills/planning/` |
| `claude/skills/resolve-reviews` | `.claude/skills/resolve-reviews/` |
| `claude/skills/security-checklist` | `.claude/skills/security-checklist/` |
| `claude/skills/systematic-debugging` | `.claude/skills/systematic-debugging/` |
| `claude/skills/test` | `.claude/skills/test/` |
| `claude/skills/write-pr` | `.claude/skills/write-pr/` |
| `claude/skills/the-sdk` | `.claude/skills/the-sdk/` |
| `claude/skills/java-spring-arch` | `.claude/skills/java-spring-arch/` |
| `claude/skills/nextjs-fsd-architecture` | `.claude/skills/nextjs-fsd-architecture/` |
| `claude/skills/tailwind-shadcn` | `.claude/skills/tailwind-shadcn/` |
| `claude/skills/data-and-forms` | `.claude/skills/data-and-forms/` |
| `claude/skills/react-conventions` | `.claude/skills/react-conventions/` |
| `claude/agents/contradiction-finder` | `.claude/agents/contradiction-finder.md` |
| `claude/agents/kotlin-convention-validator` | `.claude/agents/kotlin-convention-validator.md` |
| `claude/agents/frontend-convention-validator` | `.claude/agents/frontend-convention-validator.md` |
| `claude/agents/doc-polisher` | `.claude/agents/doc-polisher.md` |
| `claude/agents/prompt-polisher` | `.claude/agents/prompt-polisher.md` |
| `claude/agents/kotlin-test-fixer` | `.claude/agents/kotlin-test-fixer.md` |
| `claude/agents/web-researcher` | `.claude/agents/web-researcher.md` |

### claude 훅 (opt-in 전용)

훅 사용 시 `dispatcher`와 `settings`는 **반드시** 함께 활성화해야 합니다.  
각 모듈 설명 및 추천 조합은 [훅 레퍼런스](./reference/hooks.md)를 참고하세요.

`claude/settings`만 활성화하면 훅이 없는 기본 설정이 배포됩니다. 하나 이상의 Claude 훅을 함께 활성화하면 훅 등록이 포함된 설정이 배포됩니다.

| ID                           | 배포 경로                                  |
|------------------------------|----------------------------------------|
| `claude/hooks/dispatcher`    | `.claude/hooks/`                       |
| `claude/settings`            | `.claude/settings.json`                |
| `claude/hooks/logging`       | `.claude/hooks/modules/logging/`       |
| `claude/hooks/command-guard` | `.claude/hooks/modules/command-guard/` |
| `claude/hooks/secret-guard`  | `.claude/hooks/modules/secret-guard/`  |
| `claude/hooks/ktlint`        | `.claude/hooks/modules/ktlint/`        |
| `claude/hooks/gradle-test`   | `.claude/hooks/modules/gradle-test/`   |
| `claude/hooks/spotless`      | `.claude/hooks/modules/spotless/`      |
| `claude/hooks/biome`         | `.claude/hooks/modules/biome/`         |
| `claude/hooks/eslint`        | `.claude/hooks/modules/eslint/`        |
| `claude/hooks/prettier`      | `.claude/hooks/modules/prettier/`      |
| `claude/hooks/ts-check`      | `.claude/hooks/modules/ts-check/`      |
| `claude/hooks/jest`          | `.claude/hooks/modules/jest/`          |
| `claude/hooks/vitest`        | `.claude/hooks/modules/vitest/`        |
| `claude/hooks/ruff`          | `.claude/hooks/modules/ruff/`          |
| `claude/hooks/ponytail`      | `.claude/hooks/modules/ponytail/`      |

### Next.js FSD 프로젝트 파일 (opt-in 전용)

기존 프로젝트 루트 설정을 덮어쓸 수 있으므로 Next.js FSD 프로젝트에서만 활성화하세요. 자세한 구조와 설치 방법은 [프론트엔드 아키텍처](./architecture/frontend.md)를 참고하세요.

| ID | 배포 경로 |
|----|----------|
| `nextjs/fsd/steiger-config` | `steiger.config.mjs` |
| `nextjs/fsd/dependency-check` | `scripts/check-fsd-dependencies.mjs` (v1 `views` 레이아웃 전용) |

### React 스킬 (opt-in 전용)

React/Next.js 프로젝트에서만 필요하므로 기본 그룹에 포함하지 않습니다. 사용할 런타임에 맞춰 `overrides`에서 `true`로 켜세요.

| ID | 배포 경로 |
|----|----------|
| `claude/skills/react-best-practices` | `.claude/skills/react-best-practices/` |
| `codex/skills/react-best-practices` | `.agents/skills/react-best-practices/` |

```yaml
overrides:
  claude/skills/react-best-practices: true
  codex/skills/react-best-practices: true
```

### codex 그룹

| ID | 배포 경로 |
|----|----------|
| `codex/skills/api-design` | `.agents/skills/api-design/` |
| `codex/skills/database-schema` | `.agents/skills/database-schema/` |
| `codex/skills/docker` | `.agents/skills/docker/` |
| `codex/skills/git-commit` | `.agents/skills/git-commit/` |
| `codex/skills/humanizer` | `.agents/skills/humanizer/` |
| `codex/skills/ponytail` | `.agents/skills/ponytail/` |
| `codex/skills/ponytail-audit` | `.agents/skills/ponytail-audit/` |
| `codex/skills/kotest-guide` | `.agents/skills/kotest-guide/` |
| `codex/skills/kotlin-spring-arch` | `.agents/skills/kotlin-spring-arch/` |
| `codex/skills/migration-guide` | `.agents/skills/migration-guide/` |
| `codex/skills/planning` | `.agents/skills/planning/` |
| `codex/skills/resolve-reviews` | `.agents/skills/resolve-reviews/` |
| `codex/skills/security-checklist` | `.agents/skills/security-checklist/` |
| `codex/skills/systematic-debugging` | `.agents/skills/systematic-debugging/` |
| `codex/skills/test` | `.agents/skills/test/` |
| `codex/skills/write-pr` | `.agents/skills/write-pr/` |
| `codex/skills/the-sdk` | `.agents/skills/the-sdk/` |
| `codex/skills/java-spring-arch` | `.agents/skills/java-spring-arch/` |
| `codex/skills/nextjs-fsd-architecture` | `.agents/skills/nextjs-fsd-architecture/` |
| `codex/skills/tailwind-shadcn` | `.agents/skills/tailwind-shadcn/` |
| `codex/skills/data-and-forms` | `.agents/skills/data-and-forms/` |
| `codex/skills/react-conventions` | `.agents/skills/react-conventions/` |
| `codex/agents/contradiction-finder` | `.codex/agents/contradiction-finder.toml` |
| `codex/agents/kotlin-convention-validator` | `.codex/agents/kotlin-convention-validator.toml` |
| `codex/agents/frontend-convention-validator` | `.codex/agents/frontend-convention-validator.toml` |
| `codex/agents/doc-polisher` | `.codex/agents/doc-polisher.toml` |
| `codex/agents/prompt-polisher` | `.codex/agents/prompt-polisher.toml` |
| `codex/agents/kotlin-test-fixer` | `.codex/agents/kotlin-test-fixer.toml` |
| `codex/agents/web-researcher` | `.codex/agents/web-researcher.toml` |
| `codex/config` | `.codex/config.toml` |
| `codex/hooks/dispatcher` | `.codex/hooks/` |
| `codex/hooks-json` | `.codex/hooks.json` |
| `codex/hooks/logging` | `.codex/hooks/modules/logging/` |
| `codex/hooks/command-guard` | `.codex/hooks/modules/command-guard/` |
| `codex/hooks/secret-guard` | `.codex/hooks/modules/secret-guard/` |
| `codex/hooks/ktlint` | `.codex/hooks/modules/ktlint/` |
| `codex/hooks/gradle-test` | `.codex/hooks/modules/gradle-test/` |
| `codex/hooks/spotless` | `.codex/hooks/modules/spotless/` |
| `codex/hooks/biome` | `.codex/hooks/modules/biome/` |
| `codex/hooks/eslint` | `.codex/hooks/modules/eslint/` |
| `codex/hooks/prettier` | `.codex/hooks/modules/prettier/` |
| `codex/hooks/ts-check` | `.codex/hooks/modules/ts-check/` |
| `codex/hooks/jest` | `.codex/hooks/modules/jest/` |
| `codex/hooks/vitest` | `.codex/hooks/modules/vitest/` |
| `codex/hooks/ruff` | `.codex/hooks/modules/ruff/` |
| `codex/hooks/ponytail` | `.codex/hooks/modules/ponytail/` |

### gemini 그룹

| ID | 배포 경로 |
|----|----------|
| `gemini/settings` | `.gemini/settings.json` |

## 동기화 활성화 / 비활성화 (`enabled`)

`enabled` 필드로 해당 레포의 동기화 자체를 켜고 끕니다. 기본값은 `true`입니다.

| 값 | 동작 |
|----|------|
| `true` (기본) | 평소대로 동기화 |
| `false` | 이 레포는 동기화 대상에서 제외 — sync PR도 cleanup PR도 만들어지지 않음 |

```yaml
enabled: false
```

`false`로 두면 매트릭스 산출 단계에서 아예 제외되므로 파일 비교조차 하지 않습니다.
GitHub App을 제거하지 않고 **일시적으로 동기화만 멈추고 싶을 때** 사용하세요.
필드를 생략하거나 `.harness/sync.yml` 파일 자체가 없으면 기본값 `true`가 적용됩니다.

## 동기화 브랜치 접두사 (`branch_prefix`)

`branch_prefix`는 동기화 PR 브랜치의 접두사입니다. 기본값은 `harness-sync/`이며, 실제 동기화 브랜치는 `<branch_prefix><base_branch>` 형식입니다.

예를 들어 `branch_prefix: update/`, `base_branch: develop`이면 `update/develop` 브랜치에서 PR을 만듭니다.

같은 동기화 브랜치의 PR이 이미 열려 있으면 새 동기화를 만들지 않고 건너뜁니다. 해당 PR을 머지하거나 닫은 뒤 다음 동기화 실행에서 다시 반영됩니다.

## 대상 브랜치 설정 (`base_branch`)

`base_branch`는 sync 브랜치를 **분기하는 기준이자 PR을 보낼 대상** 브랜치입니다.
생략하면 레포의 GitHub 기본 브랜치(`main` 또는 `master`)가 쓰입니다.

> ⚠️ **개발 브랜치와 기본 브랜치가 다르면 반드시 지정하세요.**
> 예를 들어 GitHub 기본 브랜치는 `master`인데 실제 개발은 `develop`에서 하고,
> `master`가 `develop`보다 뒤처져 있는 경우, `base_branch`를 생략하면 동기화가
> 뒤처진 `master` 기준으로 분기한 뒤 PR만 `develop`으로 향하게 됩니다.
> 그러면 `develop`에 **이미 반영된 내용이 phantom diff로 PR에 계속 올라옵니다.**
> 이때는 `base_branch: develop`으로 명시하면 됩니다.

```yaml
base_branch: develop
```

이렇게 하면 sync 브랜치가 `develop`에서 분기되므로, `develop`과 내용이 같으면
diff가 없어 **PR이 아예 생성되지 않고**, 실제로 다를 때만 정확한 PR이 열립니다.

## PR 언어 설정

`language` 필드로 sync PR의 제목과 본문 언어를 지정합니다. 기본값은 `en`(영어)입니다.

| 값 | PR 제목 | PR 본문 |
|----|---------|---------|
| `en` (기본) | `Sync files from AI harness` | 영어 |
| `ko` | `[global] {가장 규모가 큰 커밋의 ': ' 이후 텍스트}` | 한국어 |

`ko` 설정 시 제목은 해당 sync를 유발한 커밋 중 `harness/`를 바꾼 일반 커밋(머지 커밋 제외)에서 `harness/` 변경 파일 수가 가장 많은 커밋의 메시지에서 `: ` 이후 부분을 추출합니다.

예시: `update(global): write-pr 스킬 개선` 커밋이 `master`에 반영되면 → PR 제목 `[global] write-pr 스킬 개선`

```yaml
language: ko
```

## PR 라벨 설정

`pr_label` 필드로 sync PR에 붙는 `harness sync:하네스 동기화` 라벨 부착 여부를 제어합니다. 기본값은 `true`(라벨 부착)입니다.

| 값 | 동작 |
|----|------|
| `true` (기본) | sync PR과 cleanup PR에 `harness sync:하네스 동기화` 라벨을 붙임 |
| `false` | 라벨을 붙이지 않음 |

타깃 레포에 해당 라벨이 없거나, 레포 자체 라벨 정책으로 관리하고 싶을 때 `false`로 끄면 됩니다. 이 설정은 일반 sync PR과 구 파일 정리(cleanup) PR 양쪽 모두에 적용됩니다.

```yaml
pr_label: false
```

## 버전 고정

`overrides` 값에 버전 문자열을 지정하면 아카이브된 버전으로 배포됩니다.

```yaml
overrides:
  claude/skills/git-commit: "v1"  # v1 아카이브 고정
```

아카이브가 없는 항목에 문자열을 지정하면 sync 시 경고 후 건너뜁니다.

현재 아카이브된 버전은 다음과 같습니다.

| 항목 | 버전 | 내용 |
|------|------|------|
| `claude/skills/the-sdk`, `codex/skills/the-sdk` | `v1` | the-sdk 1.5(JitPack, Spring Boot 3.x) 기준 안내. 최신 스킬은 the-sdk 2.0(Maven Central, Spring Boot 4.0.x) 기준입니다 |
| `claude/skills/nextjs-fsd-architecture`, `codex/skills/nextjs-fsd-architecture` | `v1` | `src/app` + `views` 레이아웃 기준 안내. 최신 스킬은 FSD 공식 Next.js 구조(`src/_app`, `src/_pages`) 기준입니다 |
| `nextjs/fsd/steiger-config` | `v1` | `views` 레이아웃용 steiger 설정 |
| `claude/agents/frontend-convention-validator`, `codex/agents/frontend-convention-validator` | `v1` | `views` 레이아웃 기준 검사 항목 |

Spring Boot 3.x를 쓰며 the-sdk 1.x에 머무는 레포는 아래처럼 `v1`으로 고정합니다.

```yaml
overrides:
  claude/skills/the-sdk: "v1"
  codex/skills/the-sdk: "v1"
```

아직 `views` 레이아웃을 쓰는 Next.js FSD 레포는 공식 구조로 옮기기 전까지 아래처럼 고정합니다. 옮기는 방법은 [프론트엔드 아키텍처](./architecture/frontend.md)의 "v1에서 옮기기" 절를 참고하세요.

```yaml
overrides:
  claude/skills/nextjs-fsd-architecture: "v1"
  codex/skills/nextjs-fsd-architecture: "v1"
  claude/agents/frontend-convention-validator: "v1"
  codex/agents/frontend-convention-validator: "v1"
  nextjs/fsd/steiger-config: "v1"
  nextjs/fsd/dependency-check: true
```

`nextjs-turborepo-fsd`, `nextjs-package-boundaries`, `tanstack-query-zod` 스킬은 v2에서 없어졌으므로 고정할 수 없습니다. 앞의 두 스킬은 `nextjs-fsd-architecture`의 Turborepo 절로 합쳐졌고, `tanstack-query-zod`는 `data-and-forms`로 이름이 바뀌었습니다.

## 예시 — 프론트엔드 (TypeScript)

```yaml
groups:
  - claude
  - codex

overrides:
  claude/skills/kotest-guide: false
  claude/skills/kotlin-spring-arch: false
  claude/skills/migration-guide: false
  claude/skills/database-schema: false
  claude/skills/the-sdk: false
  claude/skills/java-spring-arch: false
  codex/skills/kotest-guide: false
  codex/skills/kotlin-spring-arch: false
  codex/skills/migration-guide: false
  codex/skills/database-schema: false
  codex/skills/the-sdk: false
  codex/skills/java-spring-arch: false
  claude/hooks/dispatcher: true
  claude/settings: true
  claude/hooks/secret-guard: true
  claude/hooks/command-guard: true
  claude/hooks/biome: true
  claude/hooks/ts-check: true
  codex/hooks/dispatcher: true
  codex/hooks-json: true
  codex/hooks/secret-guard: true
  codex/hooks/command-guard: true
  nextjs/fsd/steiger-config: true
```

## 예시 — Kotlin / Spring Boot 백엔드

```yaml
groups:
  - claude
  - codex
  - gemini

overrides:
  claude/hooks/dispatcher: true
  claude/settings: true
  claude/hooks/secret-guard: true
  claude/hooks/command-guard: true
  claude/hooks/ktlint: true
  claude/hooks/gradle-test: true
  codex/hooks/dispatcher: true
  codex/hooks-json: true
  codex/hooks/secret-guard: true
  codex/hooks/command-guard: true
  codex/hooks/ktlint: true
  codex/hooks/gradle-test: true

branch_prefix: update/
base_branch: develop
```
