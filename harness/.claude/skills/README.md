# Skills

Claude에서 `/skill-name` 으로 호출하는 재사용 가능한 작업 단위입니다.

스킬은 `sync-manifest.yml`의 `domain` 값(`frontend` / `backend` / `common`)을 기준으로 나눕니다. 대시보드의 동기화 항목 선택 화면도 같은 기준으로 묶어서 보여줍니다.

## Frontend

| 스킬 | 설명 |
| --- | --- |
| [nextjs-fsd-architecture](./nextjs-fsd-architecture/SKILL.md) | Next.js App Router + FSD 공식 구조 — `_app`·`_pages` 레이어, 슬라이스 세그먼트, 공개 API, 라우트 파일, Steiger, Turborepo 패키지 경계 |
| [tailwind-shadcn](./tailwind-shadcn/SKILL.md) | Tailwind v4 클래스, `cn()`, `@theme` 토큰, 공통 UI(`shared/ui`)·도메인 UI 소유권 |
| [react-conventions](./react-conventions/SKILL.md) | React·TypeScript 이름 규칙, 타입 작성, 컴포넌트 모양·export, 컴포넌트 안 순서, import 순서 |
| [data-and-forms](./data-and-forms/SKILL.md) | TanStack Query key·훅, RSC prefetch, 무효화 범위, Zod·React Hook Form 폼과 API 에러 표시 |
| [react-best-practices](./react-best-practices/SKILL.md) | Vercel의 React/Next.js 성능 규칙 70개 — 워터폴, 번들 크기, 리렌더링 등 영향도 순 (opt-in) |

## Backend

| 스킬 | 설명 |
| --- | --- |
| [api-design](./api-design/SKILL.md) | 새 엔드포인트 REST 설계 — URL 구조, `@RequestParam` vs `@ModelAttribute`, OpenAPI 어노테이션, `CommonApiResponse` |
| [kotlin-spring-arch](./kotlin-spring-arch/SKILL.md) | Kotlin + Spring 계층 책임, `@Transactional` 전략, `ExpectedException`, Entity↔DTO 변환 패턴 참조 |
| [java-spring-arch](./java-spring-arch/SKILL.md) | Java + Spring Boot 4.0 계층 책임, `@Transactional` 전략(readOnly, N+1 방지), `ExpectedException`, Entity↔DTO 변환 |
| [database-schema](./database-schema/SKILL.md) | 테이블 명명, 컬럼 규칙, 인덱스 전략, JPA 엔티티 매핑 패턴 |
| [migration-guide](./migration-guide/SKILL.md) | DB 스키마 변경·Entity 수정 영향 분석, 올바른 변경 순서(Entity → DTO → Repository → Service → 테스트), 2단계 컬럼 삭제 |
| [kotest-guide](./kotest-guide/SKILL.md) | Kotest + MockK 패턴 — Given/When/Then 구조, mock 생성, stubbing, 코루틴 테스트, 예외 검증 (Kotlin 전용) |
| [the-sdk](./the-sdk/SKILL.md) | the-sdk 2.0(Spring Boot 4.0.x) 사용법 — Log-ID 기반 요청/응답 로깅, `CommonApiResponse`, `ExpectedException`, Swagger 자동 설정 |
| [security-checklist](./security-checklist/SKILL.md) | 하드코딩 시크릿·SQL 인젝션·JWT 검증·API 키 마스킹·민감 로깅·인가 검사. auth·API 관련 PR 머지 전 필수 |

## 공통

| 스킬 | 설명 |
| --- | --- |
| [git-commit](./git-commit/SKILL.md) | 변경사항을 논리적 단위로 분리해 컨벤션에 맞는 커밋 생성. Git Flow 자동 감지(develop 브랜치 체크) |
| [write-pr](./write-pr/SKILL.md) | base 브랜치 이후 커밋 기반으로 PR 제목·본문·라벨 생성 및 GitHub PR 오픈까지 자동화 |
| [resolve-reviews](./resolve-reviews/SKILL.md) | 미해결 PR 리뷰 스레드 수집 후 컨벤션 기준으로 판단 — 유효하면 자동 반영, 무효하면 반박 댓글 작성 |
| [planning](./planning/SKILL.md) | 숨겨진 요구사항·트레이드오프·제약을 발굴하는 구조화된 인터뷰 후 상세 구현 스펙 파일 생성 |
| [systematic-debugging](./systematic-debugging/SKILL.md) | 버그·테스트 실패·예상 외 동작 발생 시 수정 제안 전에 실행. 근본 원인 추적 방법론 적용 |
| [test](./test/SKILL.md) | 컨텍스트 기반으로 테스트 범위(단일/모듈/전체) 결정 후 실행, 커버리지 분석 및 실패 상세 보고 |
| [ponytail](./ponytail/SKILL.md) | 필요한 만큼만 코드를 쓰게 함. 기존 코드·표준 라이브러리·설치된 의존성을 먼저 쓰고, 코드를 바꾼 응답 끝에 확인하지 못한 부분과 리스크를 적음 |
| [ponytail-audit](./ponytail-audit/SKILL.md) | 레포 전체를 읽고 버그·보안·부하·테스트 누락·성능·지울 코드를 우선순위 리포트로 정리. 코드는 고치지 않음 |
| [humanizer](./humanizer/SKILL.md) | 의미와 사실관계를 유지하면서 AI 특유의 문체를 자연스러운 글로 다듬기 |
| [docker](./docker/SKILL.md) | Dockerfile·docker-compose 작성 — 멀티 스테이지 빌드, 레이어 캐싱, 보안 기본값, 서비스 연결 |
