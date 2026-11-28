---
title: 스킬 레퍼런스
description: 스킬 목록과 역할
order: 130
---

# Skills Reference

Claude Code에서 `/skill-name` 으로 직접 호출하는 스킬 목록입니다.

> 각 스킬은 `.claude/skills/<name>/` 에 위치하며, Claude가 컨텍스트를 보고 자동 제안하기도 합니다.
> 분류는 `sync-manifest.yml`의 `domain` 값(`frontend` / `backend` / `common`)을 따르며, 대시보드 동기화 항목 화면도 같은 기준으로 묶어 보여 줍니다.

---

## Frontend (Next.js / FSD)

| 스킬 | 호출 | 설명 |
|------|------|------|
| `nextjs-fsd-architecture` | `/nextjs-fsd-architecture` | FSD 공식 Next.js 구조(`_app`, `_pages`), 레이어 고르기, slice·세그먼트, `index.server.ts`·`@x` 공개 API, 라우트 파일(얇은 오류·404 라우트와 독립 global-error), 조건부 공개 모킹 flag 예외, Steiger, Turborepo 패키지 경계와 entity 승격 |
| `tailwind-shadcn` | `/tailwind-shadcn` | Tailwind v4 클래스와 `cn()`, `@theme` 토큰, 공통 UI(`shared/ui`)·도메인 UI 소유권 |
| `react-conventions` | `/react-conventions` | 파일·타입 이름(`Props`, `Request`, `Schema`, 종류 접미사 없는 타입), `interface`/`type`, `enum` 대신 union, 화살표 함수 컴포넌트와 named export(default export는 라우트 파일만), 컴포넌트 안 순서, `'use client'` 범위, import 순서. **(lint)**는 대상 레포 설정에 규칙이 켜져 있을 때만 자동 검사로 인정 |
| `data-and-forms` | `/data-and-forms` | 실제 HTTP 오류·401 계약 확인, typed API wrapper, TanStack Query `queryOptions` 팩토리·mutation 훅 이름, RSC prefetch와 `HydrationBoundary`, 무효화 범위, Zod·React Hook Form 폼과 API 에러 표시 |
| `react-best-practices` | `/react-best-practices` | React/Next.js 성능 규칙 70개. 요청 워터폴, 번들 크기, 서버 성능, 리렌더링, 렌더링, JavaScript 성능을 영향도 순으로 정리 (opt-in) |

`react-best-practices`는 [vercel-labs/agent-skills](https://github.com/vercel-labs/agent-skills/tree/main/skills/react-best-practices) v1.0.0(MIT)을 옮겨 온 스킬입니다. 합본 문서(`AGENTS.md`)와 빌드용 파일은 빼고 규칙 파일만 가져왔습니다. FSD slice의 `index.ts` 공개 API, TanStack Query 등 프로젝트 컨벤션과 충돌하면 프로젝트 컨벤션을 따르도록 했습니다. 기본 그룹에 포함되지 않으므로 [동기화 설정](../sync-configuration.md#react-스킬-opt-in-전용)에서 직접 켜야 합니다.

설치와 프로젝트 구조는 [프론트엔드 아키텍처](../architecture/frontend.md)를 참고하세요.

## Backend (Kotlin / Java / Spring Boot)

| 스킬 | 호출 | 설명 |
|------|------|------|
| `kotlin-spring-arch` | `/kotlin-spring-arch` | Controller·Service·Repository 레이어 책임, `@Transactional` 전략(readOnly 최적화, N+1 방지), `ExpectedException` 사용법, Entity↔DTO 변환 패턴 |
| `kotest-guide` | `/kotest-guide` | Kotest + MockK 테스트 패턴. Given/When/Then 구조, mock 생성, stubbing, argument capture, 코루틴 테스팅, 예외 검증 |
| `migration-guide` | `/migration-guide` | DB 스키마 변경 및 Entity 수정 시 올바른 변경 순서(Entity → DTO → Repository → Service → Tests), JPA DDL 전략, 컬럼 2단계 삭제 |
| `api-design` | `/api-design` | RESTful URL 구조, `@RequestParam` vs `@ModelAttribute` 바인딩 규칙, OpenAPI 어노테이션, `CommonApiResponse` 사용법 |
| `database-schema` | `/database-schema` | 테이블·컬럼 네이밍 컨벤션, 인덱스 전략, JPA 엔티티 매핑 패턴 |
| `java-spring-arch` | `/java-spring-arch` | Java + Spring Boot 4.0 레이어 구조 및 트랜잭션·DTO 패턴 가이드, `ExpectedException` 사용법, Entity↔DTO 변환 패턴 |
| `the-sdk` | `/the-sdk` | the-sdk 2.0(Spring Boot 4.0.x, Maven Central) 공통 라이브러리 가이드. Logging(`Log-ID` 자동 부여), `CommonApiResponse` 래핑, `ExpectedException` 처리, Swagger 자동 구성 |
| `security-checklist` | `/security-checklist` | 하드코딩 시크릿, SQL 인젝션, JWT 검증, API 키 마스킹, 민감 로깅, 인가 체크 등 보안 취약점 검증. auth·API 관련 변경 전 실행 |

## 공통

### 개발 워크플로

| 스킬 | 호출 | 설명 |
|------|------|------|
| `git-commit` | `/git-commit` | 변경사항을 논리 단위로 분리해 커밋 생성. `develop` 브랜치 감지 시 feature 브랜치 자동 체크아웃 |
| `write-pr` | `/write-pr` | 베이스 브랜치 이후 커밋을 분석해 PR 제목·본문·라벨 자동 생성 후 GitHub PR 오픈 |
| `planning` | `/planning [instructions]` | 요구사항 인터뷰를 진행하고 구현 스펙 문서(`.md`)를 생성 |
| `resolve-reviews` | `/resolve-reviews` | 열린 PR의 미해결 리뷰 스레드를 수집해 유효한 것은 자동 적용, 무효한 것은 반박 댓글 작성 |

### 코드 품질

| 스킬 | 호출 | 설명 |
|------|------|------|
| `systematic-debugging` | `/systematic-debugging` | 버그·테스트 실패·예상 외 동작 발생 시 픽스 전에 먼저 실행. 원인 분석 → 가설 수립 → 검증 순서로 안내 |
| `test` | `/test` | 테스트 실행 범위(단일 테스트 / 모듈 / 전체)를 컨텍스트로 판단하고 커버리지 결과 보고 |
| `ponytail` | `/ponytail` | 작업을 끝내는 데 필요한 만큼만 코드를 쓰게 함. 기존 코드, 표준 라이브러리, 설치된 의존성을 먼저 찾고 요청하지 않은 추상화나 옵션은 만들지 않음. 코드를 바꾼 응답 끝에 확인하지 못한 부분과 리스크를 적음 |
| `ponytail-audit` | `/ponytail-audit [경로]` | 레포 전체나 지정 경로를 읽고 버그, 보안, 부하, 테스트 누락, 성능, 지울 수 있는 코드를 우선순위별 리포트로 정리. 코드는 고치지 않음 |

`ponytail`과 `ponytail-audit`은 [DietrichGebert/ponytail](https://github.com/DietrichGebert/ponytail) v5.2.0(MIT)을 옮겨 온 스킬입니다. 원본의 레벨 전환(lite/ultra)과 `shortcut:` 주석 규칙은 뺐고, 프로젝트 지침과 충돌하면 프로젝트 지침을 따르도록 했습니다. 스킬 파일만 두면 모델이 알아서 불러오는 일이 드물어서, 세션마다 규칙을 적용하려면 [`ponytail` 훅](./hooks.md#ponytail-sessionstart)을 함께 켜야 합니다.

### 문서 및 글쓰기

| 스킬 | 호출 | 설명 |
|------|------|------|
| `humanizer` | `/humanizer` | 의미와 사실관계를 유지하면서 AI 특유의 과장, 정형화된 문장 구조, 장식적 표현을 자연스러운 문체로 다듬기 |

### 인프라

| 스킬 | 호출 | 설명 |
|------|------|------|
| `docker` | `/docker` | Dockerfile·docker-compose 작성 가이드. 멀티스테이지 빌드, 레이어 캐싱, 보안 best practice, compose 서비스 연결 |
