---
title: 에이전트 레퍼런스
description: 에이전트 트리거와 역할
order: 110
---

# Agents Reference

트리거 문구를 입력하면 Claude(또는 Codex)가 자동으로 위임하는 서브에이전트 목록입니다.  
직접 이름을 언급해 호출할 수도 있습니다.  
분류는 `sync-manifest.yml`의 `domain` 값(`frontend` / `backend` / `common`)을 따릅니다.

> 각 에이전트는 Claude용 `.claude/agents/<name>.md`와 Codex용 `.codex/agents/<name>.toml`로 제공됩니다.
> 역할은 같지만 도구별 실행 지침과 헤더 포맷이 다를 수 있습니다 — Claude는 YAML frontmatter, Codex는 TOML(`name`/`description`/`developer_instructions` + `sandbox_mode`·`model_reasoning_effort`). 두 디렉토리는 독립 시스템이라 각각 유지됩니다.
> 작성 규칙은 [Claude 컨벤션](../conventions/claude.md) / [Codex 컨벤션](../conventions/codex.md)를 참고하세요.

---

## 공통 지침 처리

규칙을 참조하는 에이전트는 작업 경로에 적용되는 `AGENTS.md`와 존재하는 규칙 문서만 읽습니다. `AGENTS.md`가 없으면 다른 문서를 참고하며, 없는 파일을 만들거나 인용하지 않습니다. 하위 지침의 적용 범위와 문서 간 충돌 처리 기준은 [전역 컨벤션](../conventions/global.md#프로젝트-지침-agentsmd)을 참고하세요.

문서가 전혀 없으면 `contradiction-finder`는 문서 기준 검사(L1–L3)의 한계를 보고하고 가능한 에이전트 간 검사(L4)를 계속합니다. `kotlin-convention-validator`는 명시된 규칙이나 도구 설정으로 뒷받침되지 않는 스타일 변경을 제안으로 남깁니다.

## Frontend

### frontend-convention-validator
**TypeScript/TSX 전용 읽기 검사기**입니다. FSD 공식 Next.js 구조(`_app`, `_pages`) 기준으로 lint·Steiger·타입 검사·포맷 결과를 먼저 인용하고, 도구가 잡지 못하는 라우트 파일 두께와 오류·404·global-error 구조, 레이어 배치, 서버/클라이언트 경계(모킹 flag 예외 포함), API 에러 계약·401 refresh 처리, TanStack Query/Zod 폼, Tailwind/shadcn 소유권과 접근성, Turborepo 앱별 공통 정책 적용을 검사합니다. **(lint)** 규칙은 대상 레포 설정에 켜져 있고 명령이 실행됐을 때만 자동 검사로 인정하며, 읽기 전용이라 운영 빌드처럼 직접 실행하지 못한 검사는 따로 표시합니다.

근거(파일·줄·규칙)를 댈 수 없는 항목은 위반이 아니라 "확인 필요"로 보고합니다. 파일을 수정하거나 커밋하지 않습니다. `views` 레이아웃 레포는 `v1`으로 고정해 이전 검사 항목을 유지할 수 있습니다.

**트리거**: `프론트엔드 컨벤션 검사해줘` / `frontend-convention-validator 실행해`

---

## Backend

### kotlin-convention-validator
**Kotlin 전용.** `git diff HEAD` 기준 변경된 `.kt` 파일에서 컨벤션 위반을 감지하고 자동 수정합니다.  
변경된 Kotlin 파일이 없으면 즉시 종료됩니다.

검사 항목: DTO 어노테이션 타겟(`@field:` vs `@param:`), 로깅 스타일, `ExpectedException` 메시지 형식, `val/var` 사용, 생성자 주입, `@Transactional` 위치  
수정 후 프로젝트에 Gradle wrapper와 `ktlintFormat` 작업이 있으면 최종 포맷을 정리합니다.

**트리거**: `컨벤션 검사해줘` / `kotlin-convention-validator 실행해`

---

### kotlin-test-fixer
**Kotlin 전용.** Kotlin/Kotest 테스트를 실행하고 **실패를 진단·수정**합니다. 서비스 코드가 진실의 원천 — 서비스 동작이 바뀌면 테스트를 업데이트하고, 서비스 버그(NPE, 잘못된 로직)는 서비스에서 수정합니다. Gradle 모듈 구조를 동적으로 감지하며 최대 3회 재시도 후 결과 보고.

**트리거**: `테스트 고쳐줘` / `kotlin-test-fixer 실행해줘` / `<모듈명> 테스트 고쳐줘`

---

## 공통

### contradiction-finder
문서·코드·에이전트 간 **모순·충돌을 감사**합니다. 4계층으로 검사합니다.

| 계층 | 검사 내용 |
|------|----------|
| doc ↔ doc | `AGENTS.md`, `.claude/rules/**`, `.gemini/styleguide.md`, `CONTRIBUTING.md`, `.github/copilot-instructions.md` 간 충돌 규칙 |
| doc ↔ code | 문서에 명시된 규칙이 실제 `.kt` 파일에서 지켜지는지 |
| doc ↔ agent/skill | 에이전트·스킬 정의가 적용 가능한 문서 규칙을 반영하는지 |
| agent ↔ agent | 에이전트 간 트리거 조건 중복·범위 충돌 |

**트리거**: `모순 찾아줘` / `충돌 검사해줘` / `일관성 검사해줘` / `contradiction-finder 실행해`

---

### doc-polisher
프로젝트 **문서 파일을 업데이트하고 정리**합니다. 코드 스니펫을 최신 `.kt` 파일 패턴으로 갱신하고, 설명을 단순화하며, 누락된 컨벤션을 추가합니다.

대상은 존재하는 `AGENTS.md`, `CONTRIBUTING.md`, `.gemini/styleguide.md`, `.github/copilot-instructions.md`, `.claude/agents/*.md`, `.claude/skills/**/*.md`, `.agents/skills/**/*.md`입니다. Codex 에이전트는 `.codex/agents/*.toml`도 점검합니다.

**트리거**: `문서 갱신해줘` / `문서 정리해줘` / `doc-polisher 실행해` / 특정 문서 파일명 언급

---

### prompt-polisher
에이전트·스킬 **프롬프트 파일의 개선안을 Before/After diff 형식으로 제안**합니다. 파일을 직접 수정하지는 않습니다.

검사 항목: 영어 문법·톤, frontmatter 완성도, 섹션 순서, 트리거 문구 구체성, 파일 내 중복·모순

**트리거**: `프롬프트 다듬어줘` / `에이전트 설명 다듬어줘` / `prompt-polisher 실행해`

---

### web-researcher
**최신 웹 정보를 수집**합니다. 학습 데이터 범위 밖의 릴리스 노트, 보안 권고, 라이브러리 비교, CVE 등을 검색합니다.

**트리거**: `최신 정보 조사해줘` / `web-researcher 실행해` / 최신 릴리스·CVE 관련 질문
