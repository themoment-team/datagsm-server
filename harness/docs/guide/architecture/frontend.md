---
title: 프론트엔드 아키텍처
description: Next.js App Router와 FSD 규칙
order: 100
---

# Frontend Architecture

Next.js App Router 프로젝트를 위한 Feature-Sliced Design(FSD), Turborepo, UI·데이터 계층 규칙입니다. 상세 가이드는 `nextjs-fsd-architecture`, `react-conventions`, `data-and-forms`, `tailwind-shadcn` 스킬에 있습니다.

## 적용 범위

FSD 공식 Next.js 가이드를 따릅니다. Next.js가 `src/app`과 `src/pages`를 라우터 폴더로 읽기 때문에 FSD `app`·`pages` 레이어 폴더에 밑줄을 붙입니다. Steiger는 밑줄을 떼고 레이어로 인식합니다.

```text
app/             # Next.js 라우트만 둔다 (FSD 밖, Steiger 검사 대상 아님)
src/
├── _app/        # app 레이어: provider, 전역 스타일, 초기화 설정
├── _pages/      # pages 레이어: 라우트 하나에 대응하는 화면
├── widgets/     # 여러 화면에 들어가는 큰 블록
├── features/    # 사용자 동작 하나 (폼·버튼과 mutation)
├── entities/    # 도메인 개념과 조회 (타입, query 팩토리, entity UI)
└── shared/      # 도메인을 모르는 공통 코드 (api, config, lib, ui)
```

## FSD 규칙

의존 방향은 아래와 같습니다. 한 레이어는 오른쪽 레이어만 import할 수 있습니다.

```text
_app → _pages → widgets → features → entities → shared
```

- business slice는 목적 이름 세그먼트(`ui`, `model`, `api`, `lib`, `config`)를 쓰고, 필요할 때만 만듭니다.
- slice 밖에서는 `index.ts` 공개 API로만 import합니다. 서버 전용 코드는 `index.server.ts`로 따로 공개합니다.
- 같은 레이어의 slice import는 금지합니다. 불가피한 entity 관계만 `entities/<slice>/@x/<consumer>` 공개 API로 허용합니다.
- 루트 `app/`의 라우트 파일은 아래 역할만 맡고 얇게 유지합니다. 데이터 prefetch와 `HydrationBoundary`는 `_pages`의 페이지 컴포넌트가 맡습니다.

| 라우트 파일                  | 역할                                                        |
| ---------------------------- | ----------------------------------------------------------- |
| `layout.tsx`                 | 문서의 `<html>`·`<body>`, metadata, provider, 전역 CSS 연결 |
| `page.tsx`                   | `_pages`의 페이지 재수출                                    |
| `loading.tsx`                | `_pages` 또는 `widgets`의 로딩 UI 렌더링                    |
| `error.tsx`, `not-found.tsx` | `_pages`의 오류·404 화면을 얇게 재수출                      |
| `global-error.tsx`           | `_app`의 독립적인 루트 layout 오류 경계 재수출              |
| `route.ts`                   | Route Handler                                               |

- 오류 경계 구현과 `error.tsx`·`global-error.tsx` 재수출 파일에는 `'use client'`를 둡니다. 안전한 사용자 안내와 가능한 경우 digest만 보여 주며 `error.message`나 stack은 노출하지 않습니다. 재시도 prop 이름·시그니처는 설치된 Next.js 버전의 번들 문서(`node_modules/next/dist/docs/`)에서, 문서가 없으면 설치 버전의 타입 정의에서 확인합니다.
- `global-error.tsx`는 루트 layout을 대체하므로 구현은 `src/_app`(예: `src/_app/global-error/`)에 두고 자체 `<html>`·`<body>`와 독립 스타일을 준비합니다. 루트 layout의 provider나 CSS에 의존하지 않습니다.
- 변경한 오류 화면은 안전한 출력과 재시도, 화면이 이미 제공하는 복구 경로(홈 이동 등)를 테스트합니다. 404 HTTP 상태는 없는 경로를 실제로 요청하는 E2E·통합 테스트로만 확인할 수 있습니다.
- 처음에는 `_pages` 안에서 시작하고, 두 번째 소비처가 생길 때 아래 레이어로 내립니다.

## 검증 도구

FSD 프로젝트는 [동기화 설정](../sync-configuration.md)에서 Steiger 설정을 opt-in으로 받습니다.

```yaml
overrides:
  nextjs/fsd/steiger-config: true
```

대상 프로젝트에 `steiger`와 `@feature-sliced/steiger-plugin`을 개발 의존성으로 설치한 뒤 아래 스크립트를 추가합니다.

```json
"lint:fsd": "steiger ./src --fail-on-warnings"
```

Turborepo는 설정 파일이 레포 루트 한 곳에만 배포되므로, 패키지 안에서 `steiger ./src`를 실행하면 이 설정이 적용되지 않습니다. 루트 `package.json`에 두 패키지를 설치하고 소스 루트마다 루트에서 실행합니다.

```json
"lint:fsd": "steiger apps/client/src --fail-on-warnings && steiger apps/admin/src --fail-on-warnings && steiger packages/core/src --fail-on-warnings"
```

배포되는 설정은 recommended 규칙에 `_app`·`_pages`의 `fsd/typo-in-layer-name`, `_app/providers`의 `fsd/segments-by-purpose`, Turborepo의 `packages/core/src/{entities,features,widgets}`에 대한 `fsd/insignificant-slice` 예외만 둡니다. 공식 구조에서는 Steiger가 모든 레이어를 직접 검사하므로 v1의 `check-fsd-dependencies.mjs`가 필요하지 않습니다.

### Turborepo

앱별 레이어는 `apps/*/src`, 공유 하위 레이어는 `packages/core/src`에 둡니다.

```text
apps/*/src:          _app → _pages → widgets → features → entities → shared
packages/core/src:   widgets → features → entities → shared
```

- 앱 간 직접 import는 금지하고, 패키지는 `package.json`의 `exports` 진입점으로만 사용합니다.
- `next.config` 같은 프레임워크 설정은 앱마다 따로 두되, 보안 헤더·모킹 스위치 같은 공통 정책은 필요한 모든 앱에 적용합니다. 검토할 때는 앱 목록을 확인해 빠진 앱이 없는지 봅니다.
- slice는 처음 쓰는 앱에 두고, 두 앱 이상이 데이터와 동작까지 똑같이 쓸 때만 `packages/core/src`의 같은 레이어로 옮깁니다. core는 앱을 import할 수 없으므로 아래 레이어부터(entity → feature → widget) 옮깁니다. 생김새만 같은 것(버튼 같은 기본 컴포넌트, 레이아웃 틀, 슬롯을 받는 셸)은 도메인이 없으므로 core의 `shared/ui`에 컴포넌트별 폴더로 둡니다. 레포에 이미 UI 패키지(shadcn 모노레포 등)가 있으면 그대로 쓰고, 앱끼리 컴포넌트를 나누려고 UI 패키지를 새로 만들지는 않습니다.
- core는 루트 배럴 없이 slice 단위 서브패스로 공개합니다(`@scope/core/shared/<segment>`, `@scope/core/{entities,features,widgets}/<slice>`, 각각의 `/server`).
- 앱 레이어는 core의 더 아래 레이어만 import합니다. 같은 레이어도 다른 slice라 금지입니다(앱 feature는 core feature를, 앱 widget은 core widget을 쓸 수 없고 한 레이어 위에서 조합). 앱 entity만 core entity의 `@x/<소비 slice>/index.ts`를 쓸 수 있습니다. import 경로에 레이어가 드러나므로 Steiger가 보지 못하는 이 위반을 앱 레이어별 ESLint `no-restricted-imports`로 막습니다.
- 각 앱과 `packages/core/src`에서 Steiger를 실행합니다. core에서는 앱의 참조가 보이지 않으므로 `entities`·`features`·`widgets`의 `fsd/insignificant-slice`를 끕니다(배포 설정에 포함).
- React 코드를 담는 패키지는 앱과 같은 React·hooks·접근성 lint 규칙을 쓰고, `tsconfig.json`에 `.tsx`와 `"jsx": "react-jsx"`를 둡니다.
- source-consumed workspace package만 각 앱의 `transpilePackages`에 넣습니다. 빌드된 UI package는 다시 컴파일하지 않습니다.

## UI와 데이터 규칙

- Tailwind 정적 클래스는 문자열로 쓰고, 조건부 클래스나 외부 `className` 병합에만 `cn()`을 씁니다. 색·간격은 `@theme` 토큰을 씁니다.
- 공통 primitive(shadcn 포함)는 `shared/ui`(Turborepo는 core의 `shared/ui`, 기존 UI 패키지가 있으면 그 패키지)에 컴포넌트마다 폴더와 `index.ts`를 두고, 세그먼트 전체를 묶는 배럴은 만들지 않습니다. `cn()`은 같은 위치의 `shared/lib`에 둡니다. 도메인 UI는 소유 slice에 둡니다.
- Turborepo의 Tailwind `@source`는 클래스가 들어 있는 모든 패키지(core, 기존 UI 패키지)를 가리켜야 합니다. 빠지면 빌드 오류 없이 클래스가 CSS에서 사라집니다. 컴포넌트를 담는 패키지는 앱과 같은 DOM 테스트 환경(jsdom 등)을 씁니다.
- API 호출은 프로젝트의 typed HTTP wrapper를 사용합니다. URL 상수와 `queryOptions` 기반 query 팩토리(`<리소스>Queries`)는 소유 entity의 `api/`에, mutation 훅은 동작을 맡는 feature의 `api/`에 둡니다. 컴포넌트는 팩토리 옵션을 `useQuery`에 바로 넘기고, mutation 훅은 HTTP 메서드가 아니라 동작으로 이름 짓습니다(`useUpdateProfile`).
- TanStack Query key는 `all()` 루트의 계층 factory로 만들고, mutation은 영향받는 가장 좁은 key만 무효화합니다.
- Zod schema는 `PascalCaseSchema`, 추론 request type은 `Request` 접미사를 씁니다. 그 밖의 타입에는 `Type` 같은 종류 접미사를 붙이지 않고(`User`, `UserResponse`), `src` 안의 모듈은 named export만 씁니다(default export는 `app/` 라우트와 설정 파일에만). `enum`보다 union과 `Record<Union, Metadata>`를 우선합니다.

## 환경 변수와 조건부 모킹

- 서버 환경 변수는 검증 schema 모듈을 통해서만 읽고 클라이언트가 서버 env 모듈을 import하지 않게 합니다. 새 서버 변수는 schema·환경 변수 예시·CI에 함께 반영합니다.
- 모킹이 이미 있는 프로젝트에서, 모킹을 켜고 끄는 공개 빌드 시점 flag에 한해 그 모킹의 진입 모듈에서만 실제 `process.env.NEXT_PUBLIC_*`를 literal 값과 직접 비교할 수 있습니다. 계산된 키·동적 조회·비밀 값은 예외가 아닙니다. instrumentation은 `process.env.NEXT_RUNTIME === 'nodejs'`, mock provider는 `typeof window !== 'undefined'` guard를 함께 확인합니다.
- 실제 flag 이름·활성 값·진입점과 `next.config`의 빌드 시점 기본값·비활성 분기 제거를 확인합니다. `env` 옵션으로 비밀을 노출하거나 모킹 없는 프로젝트에 모킹을 추가하지 않습니다. 빌드에 포함된 public 값·rewrite는 실행 환경 변수만 바꿔 변경할 수 없으므로 의도한 값으로 재빌드합니다.
- 서비스 워커로 모킹하는 프로젝트는 워커가 준비된 뒤에 요청을 보내고, 브라우저 응답이 실제로 워커에서 왔는지와 서버 모킹을 따로 테스트합니다. 모킹을 끈 운영 빌드에서는 워커가 등록되지 않고 클라이언트 JS 번들에 모킹 코드가 없는지 확인합니다. `public/`의 워커 스크립트는 정적 자산이라 빌드에 항상 복사되며, 등록되지 않으면 문제가 아닙니다.

## v1에서 옮기기

v1은 Next.js `src/app`을 FSD app 레이어와 함께 쓰고 `pages` 대신 `views`를 썼습니다. 옮기기 전까지는 [동기화 설정](../sync-configuration.md)의 버전 고정으로 v1 스킬·에이전트·Steiger 설정을 유지할 수 있습니다.

| v1 | v2 |
|----|----|
| `src/app/**`의 라우트, layout, route handler | 루트 `app/**` (얇게 유지) |
| `src/app`의 provider, 전역 스타일, 앱 설정 | `src/_app/{providers,styles,config}` |
| `src/views/<slice>` | `src/_pages/<slice>` |
| `scripts/check-fsd-dependencies.mjs` | 삭제. Turborepo는 ESLint 규칙으로 패키지 간 위반을 막음 |
| `@scope/core` 루트 배럴 | `@scope/core/shared/<segment>`, `@scope/core/entities/<slice>` |

- 폴더는 `git mv`로 옮겨 이력을 유지하고, `@/` import와 `tsconfig.json` `paths`, Tailwind `@source`, 테스트 glob을 함께 고칩니다.
- `.harness/sync.yml`에서 v1 고정과 `nextjs/fsd/dependency-check`를 지우고 `nextjs/fsd/steiger-config: true`만 남깁니다.
- 앱 하나를 옮길 때마다 FSD lint, 타입 검사, 빌드, 테스트를 실행합니다.

## 자동 검사 에이전트

`프론트엔드 컨벤션 검사해줘` 또는 `frontend-convention-validator 실행해`라고 요청하면 TypeScript/TSX 변경 파일을 읽기 전용으로 검사합니다. lint·Steiger·타입 검사 결과를 먼저 인용하고, 도구가 잡지 못하는 라우트 파일·오류 복구·레이어 배치·서버/클라이언트 경계와 폼·스타일 규칙을 확인합니다. **(lint)** 표시는 대상 파일에 실제 규칙이 활성화되고 명령이 실행된 경우만 자동 검사로 인정합니다. 설정되지 않은 규칙은 수동 검토로 보고하며, lint 성공만으로 검사됐다고 하지 않습니다.

스킬 목록은 [스킬 레퍼런스](../reference/skills.md), 에이전트 상세는 [에이전트 레퍼런스](../reference/agents.md)를 참고하세요.
