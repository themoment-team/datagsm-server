**한국어로 응답하고 작업해주세요.**

## 프로젝트와 모듈

DataGSM은 광주소프트웨어마이스터고등학교의 학생·동아리·프로젝트·급식·일정 정보를 제공하는 REST API 서버입니다. Google OAuth2, JWT, API 키를 사용합니다.

- 기술: Kotlin, Spring Boot 4.0, Spring Security, Spring Data JPA, QueryDSL, OpenFeign, Jackson 3.0, MySQL, Redis
- 테스트: Kotest + MockK + JUnit 5
- `datagsm-common`: 공통 Entity/DTO/Repository/Config, Health API
- `datagsm-oauth-authorization`: OAuth2 인증, 회원가입·비밀번호 재설정
- `datagsm-oauth-userinfo`: 외부 클라이언트용 OAuth2 UserInfo
- `datagsm-openapi`: 학생·동아리·프로젝트 읽기/쓰기, NEIS 연동 공개 API
- `datagsm-web`: 사용자·관리자 기능, Excel 처리
- `datagsm-shared`: Maven/npm으로 배포하는 Kotlin Multiplatform 타입 정의
- `datagsm-ksp-processor`: `datagsm-common`의 `@KmpExport`에서 KMP/TypeScript 타입 생성

서비스 모듈은 `controller/`, `service/`, `repository/`, `entity/`, `dto/` 계층을 사용합니다. 공통 Entity/DTO는 `datagsm-common/src/main/kotlin/team/themoment/datagsm/common/domain/`, 예외 처리는 같은 Kotlin 루트의 `global/common/error/`에 있습니다. `/v1/health`는 공통 모듈의 `global/controller/HealthController.kt`가 제공합니다.

## 실행 명령

Gradle 빌드에는 **Java 25**를 사용합니다.

- 빌드: `./gradlew build`
- 테스트: `./gradlew test`
- 포맷: `./gradlew ktlintFormat`
- 실행: `./gradlew :<module>:bootRun` — `datagsm-oauth-authorization`, `datagsm-oauth-userinfo`, `datagsm-openapi`, `datagsm-web`

## 핵심 코딩 규칙

- Controller → Service → Repository 구조와 생성자 주입을 사용합니다.
- `val`과 Kotlin null-safety를 우선하고, 불필요한 `var`, `!!`, 과도한 주석을 피합니다.
- Jackson은 `@field:`, Swagger는 요청 DTO에 `@param:Schema`, 응답 DTO에 `@field:Schema`를 사용합니다.
- `@Transactional`은 메서드에만 적용합니다. 읽기는 `readOnly = true`, 쓰기는 기본 트랜잭션을 사용합니다.
- 모든 API 응답에 `CommonApiResponse`를 사용하고, Fetch Join 또는 `@EntityGraph`로 N+1을 방지합니다.
- `@RequestBody`는 `reqDto`, `@ModelAttribute`는 `queryReq`를 사용합니다. 검색 의도가 명확하면 `searchReq`도 허용합니다.
- 로그는 영어 동사로 시작하고 SLF4J `{}`를 사용합니다. 문자열 보간·콜론 구분자·`println()`은 금지합니다.
- `ExpectedException`을 직접 사용합니다. 메시지는 동적 데이터 없이 한국어 합쇼체와 마침표로 작성합니다.
- `student`/`club`/`project` 쓰기 서비스는 `EventDispatchRequested`를 발행합니다. `EventDispatchConventionTest`가 검사합니다.

## 상세 규칙 읽기

파일 생성·수정·리뷰 전에 해당 규칙이 컨텍스트에 있는지 확인하고, 없다면 본문을 읽습니다. 새 파일은 예정 경로를 기준으로 판단합니다. Claude Code의 자동 로딩을 지원하지 않는 도구도 이 절차를 따릅니다.

규칙의 `paths`는 저장소 루트 기준 glob입니다. 아래 목록 외에 추가된 `.claude/rules/**/*.md`도 프론트매터를 확인하고 작업 대상과 일치하면 읽습니다. `paths`가 없는 규칙은 본문에 명시된 도구 범위에 따라 읽습니다. 일반 작업에서는 관련 규칙만 읽고, 전체 규칙 감사에서는 모두 읽습니다. 전체 규칙을 `@import`하지 않습니다.

| 작업 대상 | 상세 규칙 |
|---|---|
| `**/*.kt` | [Kotlin 스타일](.claude/rules/kotlin-style.md), [로그](.claude/rules/logging.md), [예외](.claude/rules/exception.md) |
| `**/*Dto.kt` | [DTO 어노테이션](.claude/rules/dto-annotations.md) |
| `**/*Controller.kt`, `**/*Service.kt`, `**/*ServiceImpl.kt`, `**/*Dto.kt` | [API 규칙](.claude/rules/api-conventions.md) |

**규칙 우선순위**: `AGENTS.md` > 적용 가능한 `.claude/rules/**` > `.gemini/styleguide.md` > `CONTRIBUTING.md`. 상세 규칙은 핵심 규칙을 보충하며, 충돌을 발견하면 상위 규칙을 따르고 충돌을 보고합니다.

## 테스트

- 비즈니스 로직은 Kotest `DescribeSpec`의 `describe/context/it`과 MockK로 테스트하고, `it` 내부는 Given-When-Then으로 구성합니다.
- 테스트 이름은 한국어로 작성합니다: `describe("클래스명 클래스의")`, `describe("메서드명 메서드는")`.

## 커밋

- 형식: `type(scope): 설명` — 설명은 한국어, 마침표 없이 작성합니다.
- 타입: `add` / `update` / `fix` / `refactor` / `ci/cd` / `docs` / `test` / `merge`
- 스코프: `auth`, `student`, `club`, `application` 등 도메인명. 여러 도메인에 걸친 변경만 `global`, `ci/cd`, `web`, `openapi`, `oauth`를 사용합니다.

## 작업 시 확인

- 파일 변경을 제안하거나 적용하기 전에 `.gitignore`와 `.geminiignore`가 있으면 확인합니다.
- 코드 분석과 변경 영향 검토는 멀티모듈 구조를 고려합니다.
