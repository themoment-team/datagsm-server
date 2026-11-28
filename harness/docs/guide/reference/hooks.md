---
title: 훅 레퍼런스
description: 훅 모듈과 추천 조합
order: 120
---

# Hooks Reference

세션 시작, 명령어 실행, 파일 저장 시점에 자동으로 동작하는 훅 모듈 목록입니다.

> 훅은 **opt-in** 방식입니다. `.harness/sync.yml`의 `overrides`에 명시해야 활성화됩니다.  
> 사용하려면 반드시 `dispatcher`와 `settings`(Claude) 또는 `hooks-json`(Codex)을 함께 활성화해야 합니다.

Claude Code와 Codex는 훅 시스템이 **각각 독립적**이며 ID도 분리되어 있습니다.  
같은 기능을 양쪽에서 쓰려면 `claude/hooks/*`와 `codex/hooks/*`를 **둘 다 명시**해야 합니다.

```yaml
# Claude Code 훅
overrides:
  claude/hooks/dispatcher: true  # 필수
  claude/settings: true          # 필수
  claude/hooks/<module>: true    # 원하는 모듈 추가

# Codex 훅 (Claude와 별개 — 공유되지 않음)
  codex/hooks/dispatcher: true   # 필수
  codex/hooks-json: true         # 필수
  codex/hooks/<module>: true     # 원하는 모듈 추가
```

---

## 보안 · 안전

### secret-guard `preToolUse`
파일 저장(`Write`, `Edit`) 전에 **API 키·시크릿 패턴을 감지하면 실행을 차단**합니다.  
`.env` 파일은 검사 대상에서 제외됩니다.

**적용 대상**: 모든 프로젝트

---

### command-guard `preToolUse`
`Bash` 명령어 실행 전에 **위험한 명령 패턴을 차단**합니다.

차단 패턴 예시: `rm -rf /`, `sudo rm`, `> /dev/`, `dd if=`

**적용 대상**: 모든 프로젝트

---

## 로깅

### logging `preToolUse`
모든 `Bash` 명령어를 **`.claude/command.log`에 타임스탬프와 함께 기록**합니다.  
감사 추적이나 디버깅 시 유용합니다.

**적용 대상**: 모든 프로젝트

---

## Kotlin / Java

### ktlint `postToolUse`
`.kt` 파일 저장 시 **`ktlintFormat`을 자동 실행**합니다.

**적용 대상**: Kotlin 프로젝트

---

### gradle-test `postToolUse`
`*ServiceImpl.kt` / `*ServiceImpl.java` 저장 시 **대응되는 Gradle 테스트 태스크를 자동 실행**합니다 (Kotlin·Java, JUnit·Kotest 등 언어·프레임워크 무관).
`src/` 위치를 기준으로 모듈을 판별해 단일모듈(`:test`)·멀티모듈(`:moduleA:test`)·중첩모듈(`:a:b:test`)을 모두 처리합니다.
프로젝트 루트는 `git rev-parse`로 찾고, `gradlew`가 없으면 PATH의 `gradle`로 폴백합니다.

**적용 대상**: Gradle 기반 Kotlin/Java 프로젝트

---

### spotless `postToolUse`
`.java` / `.kt` / `.groovy` 파일 저장 시 **`./gradlew spotlessApply`를 실행**합니다.

**적용 대상**: Gradle 기반 Java·Kotlin 프로젝트

---

## JavaScript / TypeScript

### biome `postToolUse`
JS/TS 파일 저장 시 **`biome check --write`를 실행**합니다.  
`biome.json`이 없으면 동작하지 않습니다.

**적용 대상**: Biome를 사용하는 JS/TS 프로젝트

---

### eslint `postToolUse`
JS/TS 파일 저장 시 **`eslint --fix`를 실행**합니다.  
ESLint 설정 파일이 없으면 동작하지 않습니다.

**적용 대상**: ESLint를 사용하는 JS/TS 프로젝트

---

### prettier `postToolUse`
JS/TS/CSS/HTML 등 저장 시 **`prettier --write`를 실행**합니다.  
Prettier 설정 파일이 없으면 동작하지 않습니다.

**적용 대상**: Prettier를 사용하는 프로젝트

---

### ts-check `postToolUse`
`.ts` / `.tsx` 파일 저장 시 **`tsc --noEmit`을 실행**해 타입 오류를 즉시 표시합니다.  
`tsconfig.json`이 없으면 동작하지 않습니다.

**적용 대상**: TypeScript 프로젝트

---

### jest `postToolUse`
JS/TS 파일 저장 시 **`jest --findRelatedTests`를 실행**합니다.  
Jest 설정 파일이 없으면 동작하지 않습니다.

**적용 대상**: Jest를 사용하는 JS/TS 프로젝트

---

### vitest `postToolUse`
JS/TS 파일 저장 시 **`vitest run`을 실행**합니다.  
Vitest 설정이 없으면 동작하지 않습니다.

**적용 대상**: Vitest를 사용하는 JS/TS 프로젝트

---

## Python

### ruff `postToolUse`
`.py` 파일 저장 시 **`ruff format` + `ruff check --fix`를 순서대로 실행**합니다.

**적용 대상**: Python 프로젝트

---

## 코딩 방식

### ponytail `sessionStart`
세션이 시작될 때 `ponytail` 스킬 규칙을 컨텍스트에 넣어, 스킬을 따로 부르지 않아도 세션 내내 적용되게 합니다. Claude Code에서는 `/clear`나 컨텍스트 압축 뒤에도 다시 넣습니다.

규칙은 스킬 파일(`.claude/skills/ponytail/SKILL.md`, Codex는 `.agents/skills/ponytail/SKILL.md`)에서 읽어 옵니다. 그래서 `claude/skills/ponytail`이나 `codex/skills/ponytail`이 함께 동기화되어 있어야 하고, 스킬이 없으면 아무것도 넣지 않습니다. 세션 중에 끄려면 "stop ponytail"이나 "normal mode"라고 말하면 됩니다.

Codex는 새로 추가된 훅을 `/hooks`에서 신뢰로 등록해야 실행합니다.

**적용 대상**: 모든 프로젝트

```yaml
overrides:
  claude/hooks/dispatcher: true
  claude/settings: true
  claude/hooks/ponytail: true
  codex/hooks/dispatcher: true
  codex/hooks-json: true
  codex/hooks/ponytail: true
```

---

## 프로젝트 유형별 추천 조합

> `claude/hooks/*`와 `codex/hooks/*`는 각각 독립적으로 활성화해야 합니다.

**Kotlin / Spring Boot**
```yaml
# Claude Code
claude/hooks/dispatcher: true
claude/settings: true
claude/hooks/secret-guard: true
claude/hooks/command-guard: true
claude/hooks/ktlint: true
claude/hooks/gradle-test: true

# Codex
codex/hooks/dispatcher: true
codex/hooks-json: true
codex/hooks/secret-guard: true
codex/hooks/command-guard: true
codex/hooks/ktlint: true
codex/hooks/gradle-test: true
```

**TypeScript (Biome)**
```yaml
# Claude Code
claude/hooks/dispatcher: true
claude/settings: true
claude/hooks/secret-guard: true
claude/hooks/command-guard: true
claude/hooks/biome: true
claude/hooks/ts-check: true

# Codex
codex/hooks/dispatcher: true
codex/hooks-json: true
codex/hooks/secret-guard: true
codex/hooks/command-guard: true
codex/hooks/biome: true
```

**Python**
```yaml
# Claude Code
claude/hooks/dispatcher: true
claude/settings: true
claude/hooks/secret-guard: true
claude/hooks/command-guard: true
claude/hooks/ruff: true

# Codex
codex/hooks/dispatcher: true
codex/hooks-json: true
codex/hooks/secret-guard: true
codex/hooks/command-guard: true
codex/hooks/ruff: true
```+
