# 기여 가이드 (Contributing Guide)

DataGSM 프로젝트에 기여해주셔서 감사합니다! 이 문서는 프로젝트 기여를 위한 가이드라인을 제공합니다.

## 목차

- [시작하기](#시작하기)
- [프로젝트 구조](#프로젝트-구조)
- [개발 환경 설정](#개발-환경-설정)
- [브랜치 전략](#브랜치-전략)
- [커밋 메시지 규칙](#커밋-메시지-규칙)
- [Pull Request 프로세스](#pull-request-프로세스)
- [코딩 컨벤션](#코딩-컨벤션)
- [테스트 작성](#테스트-작성)
- [빌드 및 실행](#빌드-및-실행)
- [CI/CD 파이프라인](#cicd-파이프라인)
- [문제 해결](#문제-해결)

## 시작하기

### 필수 요구사항

프로젝트 개발을 위해 다음 환경이 필요합니다:

- **JDK**: Java 25 (Temurin 권장)
- **Kotlin**: 2.3.0 (자동 설정)
- **Gradle**: 최신 버전 (래퍼 사용)
- **Docker & Docker Compose**: 로컬 개발용
- **MySQL**: 8.0
- **Redis**: 7-alpine
- **IDE**: IntelliJ IDEA 권장

### 프로젝트 클론

```bash
git clone https://github.com/themoment-team/datagsm-server.git
cd datagsm-server
```

### 로컬 개발 환경 설정

1. **데이터베이스 및 Redis 실행**

```bash
docker-compose -f compose.local.yaml up -d
```

이 명령은 다음을 실행합니다:
- MySQL 8.0 (포트: 3306, 데이터베이스: `datagsm`)
- Redis 7-alpine (포트: 6379)

2. **환경 변수 설정**

필요한 환경 변수를 설정합니다:

```bash
# JWT 시크릿 키 (필수)
export JWT_SECRET_KEY="your-secret-key"

# NEIS API 키 (리소스 서버용)
export NEIS_API_KEY="your-neis-api-key"

# CORS 설정
export CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:5173"

# 메일 설정 (인증 서버용)
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-app-password"
```

3. **프로젝트 빌드**

```bash
./gradlew build
```

4. **서버 실행**

```bash
# OAuth 인증 서버 (포트: 8081)
./gradlew :datagsm-oauth-authorization:bootRun

# OpenAPI 서버 (포트: 8082)
./gradlew :datagsm-openapi:bootRun

# OAuth UserInfo 서버 (포트: 8083)
./gradlew :datagsm-oauth-userinfo:bootRun

# 웹 서버 (포트: 8080)
./gradlew :datagsm-web:bootRun
```

## 프로젝트 구조

DataGSM은 멀티 모듈 프로젝트로 구성되어 있습니다:

```
datagsm-server/
├── datagsm-common/              # 공유 라이브러리
│   ├── domain/                 # Entity, Repository
│   ├── dto/                    # 공통 DTO
│   └── global/                 # Config, Exception Handling
├── datagsm-oauth-authorization/ # OAuth2 인증 서버 (포트: 8081)
│   └── domain/
│       ├── account/            # 계정 관리
│       ├── auth/               # 인증 (API Key)
│       └── oauth/              # OAuth2
├── datagsm-openapi/            # 리소스 API 서버 (포트: 8082)
│   └── domain/
│       ├── club/               # 동아리
│       ├── student/            # 학생
│       ├── neis/               # NEIS 연동 (급식, 일정)
│       └── project/            # 프로젝트
├── datagsm-oauth-userinfo/     # OAuth2 UserInfo 서버 (포트: 8083)
│   └── domain/
│       └── userinfo/           # UserInfo API
└── datagsm-web/                # 관리자 웹 API (포트: 8080)
    └── domain/
        └── excel/              # Excel 처리
```

### 모듈별 역할

| 모듈                              | 역할                                | 의존성            |
|---------------------------------|-----------------------------------|----------------|
| **datagsm-common**              | 공통 Entity, DTO, Repository, 예외 처리 | -              |
| **datagsm-oauth-authorization** | DataGSM OAuth 제공                  | datagsm-common |
| **datagsm-oauth-userinfo**      | DataGSM OAuth UserInfo 제공         | datagsm-common |
| **datagsm-openapi**             | DataGSM OpenAPI 제공                | datagsm-common |
| **datagsm-web**                 | DataGSM Web 서비스 전용 API 제공         | datagsm-common |

### 패키지 구조

모든 모듈은 다음 패키지 구조를 따릅니다:

```
team.themoment.datagsm.{module}/
├── domain/
│   └── {feature}/                 # 기능별 도메인
│       ├── controller/            # REST API 컨트롤러
│       ├── service/               # 비즈니스 로직
│       │   ├── {Service}.kt      # 인터페이스
│       │   └── impl/{ServiceImpl}.kt
│       ├── repository/            # JPA Repository
│       ├── entity/                # JPA Entity (common 제외)
│       ├── dto/
│       │   ├── request/          # *ReqDto
│       │   └── response/         # *ResDto
│       └── constant/             # 상수, Enum
└── global/
    ├── config/                    # Spring Configuration
    ├── security/                  # Security, JWT
    └── common/                    # 공통 유틸리티
```

## IDE 및 빌드 설정

### IntelliJ IDEA 설정

1. **Kotlin 플러그인 활성화**
2. **Code Style 설정**
   - Settings → Editor → Code Style → Kotlin
   - `.editorconfig` 자동 적용 확인
3. **KtLint 플러그인 설치** (선택사항)

### Gradle 캐시 활성화

프로젝트는 다음 Gradle 최적화를 사용합니다:

```properties
# gradle.properties
org.gradle.parallel=true          # 병렬 빌드
org.gradle.daemon=true            # Daemon 사용
org.gradle.caching=true           # 빌드 캐시
kotlin.incremental=true           # 증분 컴파일
```

## 브랜치 전략

프로젝트는 Git Flow 기반 브랜치 전략을 사용합니다:

```
master (프로덕션)
  ↑
develop (스테이징)
  ↑
feat/*, fix/*, refactor/*, ci/cd/*
```

### 브랜치 규칙

| 브랜치          | 목적       | Base      | Target    |
|--------------|----------|-----------|-----------|
| `master`     | 프로덕션 환경  | -         | -         |
| `develop`    | 개발 환경    | `master`  | `master`  |
| `feat/*`     | 새 기능 개발  | `develop` | `develop` |
| `fix/*`      | 버그 수정    | `develop` | `develop` |
| `refactor/*` | 코드 리팩토링  | `develop` | `develop` |
| `cicd/*`     | CI/CD 설정 | `develop` | `develop` |
| `update/*`   | 기능 개선    | `develop` | `develop` |
| `docs/*`     | 문서 수정    | `develop` | `develop` |

### 브랜치 생성 예시

```bash
# 새 기능 개발
git checkout develop
git pull origin develop
git checkout -b feat/add-meal-api

# 버그 수정
git checkout -b fix/student-search-error

# 리팩토링
git checkout -b refactor/optimize-club-query
```

## 커밋 메시지 규칙

프로젝트는 **Conventional Commits** 형식을 따르며, **한국어**로 작성합니다.

### 형식

```
{type}({scope}): {메시지}
```

### Type

| Type       | 설명          | 예시                                     |
|------------|-------------|----------------------------------------|
| `update`   | 기능 개선       | `update(auth): API 키 삭제 엔드포인트 경로 변경`   |
| `fix`      | 버그 수정       | `fix(student): 학생 검색 시 빈 문자열 처리 오류 수정` |
| `feat`     | 새 기능 추가     | `feat(neis): 급식 조회 API 추가`             |
| `refactor` | 코드 리팩토링     | `refactor(club): 동아리 조회 쿼리 최적화`        |
| `ci/cd`    | CI/CD 설정 변경 | `ci/cd(global): PR 제목 검증 규칙 추가`        |
| `docs`     | 문서 수정       | `docs(readme): 환경 설정 가이드 추가`           |
| `test`     | 테스트 추가/수정   | `test(auth): OAuth 토큰 발급 테스트 추가`       |

### Scope

모듈 및 도메인 기반으로 scope를 지정합니다:

**모듈 레벨:**
- `authorization`, `userinfo`, `resource`, `web`, `global`

**도메인 레벨:**
- `auth`, `account`, `oauth`, `club`, `student`, `neis`, `project`, `client`

### 예시

```bash
# 좋은 예시
git commit -m "update(auth): 변경된 API 키 삭제 엔드포인트의 경로 변수 이름 수정"
git commit -m "fix(global): 올바르지 않은 공개 API 경로 설정 수정"
git commit -m "feat(club): 동아리 멤버 조회 API 추가"
git commit -m "refactor(student): 학생 검색 로직 개선 및 중복 제거"

# 나쁜 예시
git commit -m "수정함"
git commit -m "fix"
git commit -m "update: updated code"
```

## Pull Request 프로세스

### PR 생성 전 체크리스트

- [ ] 코드 포맷팅 적용 (`./gradlew ktlintFormat`)
- [ ] 테스트 통과 (`./gradlew test`)
- [ ] 빌드 성공 (`./gradlew build`)
- [ ] 불필요한 주석 제거
- [ ] 환경 변수 및 시크릿 제거

### PR 제목 형식

PR 제목은 다음 형식 중 하나를 따라야 합니다:

```
[{scope}] {제목}
vYYYYMMDD.n (릴리즈용)
```

**유효한 Scope:**
- `global`, `account`, `auth`, `client`, `club`, `neis`, `oauth`, `project`, `student`
- `web`, `resource`, `authorization`, `userinfo`, `ci/cd`

**예시:**
```
[auth] API 키 관리 엔드포인트 경로 변경
[club] 동아리 멤버 조회 기능 추가
[ci/cd] PR 제목 검증 규칙 개선
v20260130.0
```

**주의:** PR 제목에는 **콜론(`:`)을 사용하지 않습니다**.

### PR 템플릿

PR을 생성하면 다음 템플릿이 자동으로 적용됩니다:

```markdown
## 개요
작업 내용 1~3 문장으로 정리

## 본문
더 자세하게 작업 내용 작성
```

### 자동 PR 처리

PR이 생성되면 다음이 자동으로 처리됩니다:

1. **라벨 추가**: `waiting for review:검토 대기`
2. **Assignee 설정**: PR 작성자 자동 지정
3. **Reviewer 지정**: 개발진 지정 (본인 제외)
4. **제목 검증**: 형식 검증 후 통과/실패 표시

### 리뷰 및 머지

1. **리뷰어 승인** 필요
2. **CI/CD 검증** 통과 필요
3. **Squash and Merge** 권장
4. **머지 후 브랜치 자동 삭제**

## 코딩 컨벤션

프로젝트는 Kotlin 및 Spring Boot 모범 사례를 따릅니다. 자세한 내용은 [CLAUDE.md](./CLAUDE.md)를 참고하세요.

### Kotlin 규칙

```kotlin
// ✅ 좋은 예시
val studentName: String = "홍길동"  // val 우선 사용
val students: List<Student> = repository.findAll()  // 명시적 타입

// ❌ 나쁜 예시
var studentName = "홍길동"  // var 지양
var students = repository.findAll()  // 타입 불명확
```

### Null Safety

```kotlin
// ✅ 좋은 예시
fun findStudent(id: Long): Student? {
    return repository.findById(id).orElse(null)
}

val name: String = student?.name ?: "Unknown"

// ❌ 나쁜 예시
fun findStudent(id: Long): Student {
    return repository.findById(id).get()  // NoSuchElementException 위험
}
```

### 레이어 구조

**Controller → Service → Repository** 패턴을 따릅니다:

```kotlin
// Controller: HTTP 요청/응답 처리
@RestController
@RequestMapping("/api/v1/club")
class ClubController(
    private val getClubService: GetClubService
) {
    @GetMapping("/{id}")
    fun getClub(@PathVariable id: Long): CommonApiResponse<ClubResDto> {
        val club = getClubService.execute(id)
        return CommonApiResponse.success(club)
    }
}

// Service: 비즈니스 로직
interface GetClubService {
    fun execute(id: Long): ClubResDto
}

@Service
class GetClubServiceImpl(
    private val clubRepository: ClubJpaRepository
) : GetClubService {
    override fun execute(id: Long): ClubResDto {
        val club = clubRepository.findById(id)
            .orElseThrow { ClubNotFoundException() }
        return ClubResDto.from(club)
    }
}

// Repository: 데이터 접근
interface ClubJpaRepository : JpaRepository<ClubJpaEntity, Long>
```

### DTO 네이밍

```kotlin
// Request DTO: *ReqDto
data class CreateClubReqDto(
    val name: String,
    val description: String
)

// Response DTO: *ResDto
data class ClubResDto(
    val id: Long,
    val name: String,
    val description: String
) {
}
```

### 생성자 주입

**생성자 주입**을 필수로 사용합니다 (Field 주입 금지):

```kotlin
// ✅ 좋은 예시: 생성자 주입
@Service
class ClubService(
    private val clubRepository: ClubJpaRepository,
    private val studentRepository: StudentJpaRepository
)

// ❌ 나쁜 예시: Field 주입
@Service
class ClubService {
    @Autowired
    lateinit var clubRepository: ClubJpaRepository
}
```

### 주석 규칙

**과도한 주석을 작성하지 않습니다**. 로직이 자명하지 않은 경우에만 주석을 추가하세요.

```kotlin
// ✅ 좋은 예시: 로직이 복잡한 경우에만 주석
fun calculateExpiryDate(createdAt: LocalDateTime, userType: UserType): LocalDateTime {
    // Admin은 365일, 일반 사용자는 30일 유효
    val days = if (userType == UserType.ADMIN) 365 else 30
    return createdAt.plusDays(days.toLong())
}

// ❌ 나쁜 예시: 불필요한 주석
// 학생을 조회하는 함수
fun findStudent(id: Long): Student {
    // ID로 학생을 찾는다
    return repository.findById(id).orElse(null)
}
```

### 코드 포맷팅

**KtLint**를 사용하여 자동 포맷팅을 적용합니다:

```bash
# 포맷팅 적용
./gradlew ktlintFormat

# 포맷팅 검증
./gradlew ktlintCheck
```

**.editorconfig** 설정:
- 인덴트: 스페이스 4칸
- 문자 인코딩: UTF-8
- 줄 끝: LF
- 파일 끝 개행: 필수

## 테스트 작성

프로젝트는 **Kotest**와 **MockK**를 사용합니다.

### 테스트 프레임워크

- **Kotest 6.0.7**: DescribeSpec 스타일 (한국어 테스트 이름 지원)
- **MockK 1.14.7**: Kotlin 전용 Mock 프레임워크
- **JUnit 5 Platform**

### 테스트 구조

**Given-When-Then** 패턴을 사용합니다:

```kotlin
class CreateClubServiceTest : DescribeSpec({
    lateinit var mockClubRepository: ClubJpaRepository
    lateinit var mockStudentRepository: StudentJpaRepository
    lateinit var createClubService: CreateClubService

    beforeEach {
        mockClubRepository = mockk<ClubJpaRepository>()
        mockStudentRepository = mockk<StudentJpaRepository>()
        createClubService = CreateClubServiceImpl(
            mockClubRepository,
            mockStudentRepository
        )
    }

    describe("CreateClubService 클래스의") {
        describe("execute 메서드는") {
            context("정상적인 동아리 생성 요청이 들어올 때") {
                it("동아리를 생성하고 ID를 반환한다") {
                    // Given
                    val reqDto = CreateClubReqDto(
                        name = "프로그래밍 동아리",
                        description = "코딩을 좋아하는 사람들의 모임"
                    )
                    val savedClub = ClubJpaEntity(
                        id = 1L,
                        name = reqDto.name,
                        description = reqDto.description
                    )

                    every { mockClubRepository.existsByName(reqDto.name) } returns false
                    every { mockClubRepository.save(any()) } returns savedClub

                    // When
                    val result = createClubService.execute(reqDto)

                    // Then
                    result shouldBe 1L
                    verify(exactly = 1) { mockClubRepository.save(any()) }
                }
            }

            context("중복된 동아리 이름으로 생성을 시도할 때") {
                it("DuplicateClubNameException을 발생시킨다") {
                    // Given
                    val reqDto = CreateClubReqDto(
                        name = "이미 존재하는 동아리",
                        description = "설명"
                    )

                    every { mockClubRepository.existsByName(reqDto.name) } returns true

                    // When & Then
                    val exception = shouldThrow<DuplicateClubNameException> {
                        createClubService.execute(reqDto)
                    }

                    exception.message shouldBe "이미 존재하는 동아리 이름입니다"
                    verify(exactly = 0) { mockClubRepository.save(any()) }
                }
            }
        }
    }
})
```

### Mock 사용법

```kotlin
// Mock 객체 생성
val mockRepository = mockk<ClubJpaRepository>()

// 메서드 동작 정의
every { mockRepository.findById(1L) } returns Optional.of(club)
every { mockRepository.save(any()) } returns savedClub
every { mockRepository.existsByName("동아리") } returns false

// void 메서드
every { mockRepository.delete(any()) } just Runs

// 예외 발생
every { mockRepository.findById(999L) } throws ClubNotFoundException()

// 호출 검증
verify(exactly = 1) { mockRepository.save(any()) }
verify(exactly = 0) { mockRepository.delete(any()) }
```

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 모듈 테스트
./gradlew :datagsm-oauth-authorization:test

# 특정 테스트 클래스 실행
./gradlew test --tests "CreateClubServiceTest"

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

## 빌드 및 실행

### 빌드 명령어

```bash
# 전체 빌드 (병렬, 캐시 활성화)
./gradlew build --parallel --build-cache

# 클린 빌드
./gradlew clean build

# 테스트 제외 빌드
./gradlew build -x test

# 특정 모듈 빌드
./gradlew :datagsm-oauth-authorization:build
```

### 환경별 실행

```bash
# 개발 환경
./gradlew :datagsm-oauth-authorization:bootRun --args='--spring.profiles.active=dev'

# 프로덕션 환경
java -jar app.jar --spring.profiles.active=prod
```

### API 문서 확인

서버 실행 후 Swagger UI로 API 문서를 확인할 수 있습니다:

- **Authorization**: http://localhost:8081/swagger-ui/index.html
- **Resource**: http://localhost:8082/swagger-ui/index.html
- **UserInfo**: http://localhost:8083/swagger-ui/index.html
- **Web**: http://localhost:8080/swagger-ui/index.html

## CI/CD 파이프라인

### CI 워크플로우

프로젝트는 GitHub Actions를 사용하여 자동화된 CI/CD를 운영합니다.

#### Stage (develop 브랜치)

- **트리거**: `develop` 브랜치에 대한 PR 및 Push
- **워크플로우**: `.github/workflows/datagsm-stage-ci.yaml`
- **작업**:
  1. PR 제목 검증
  2. PR 메타데이터 설정 (라벨, Assignee, Reviewer)
  3. JDK 25 빌드 및 테스트
  4. Discord 알림

#### Production (master 브랜치)

- **트리거**: `master` 브랜치에 대한 PR 및 Push
- **워크플로우**: `.github/workflows/datagsm-prod-ci.yaml`
- **작업**: Stage와 동일

### CD 워크플로우

CI가 성공하면 자동으로 배포가 진행됩니다:

- **Stage CD**: `.github/workflows/datagsm-stage-cd.yaml`
- **Production CD**: `.github/workflows/datagsm-prod-cd.yaml`

### 로컬에서 CI 검증

PR을 생성하기 전에 로컬에서 CI 검증을 수행하세요:

```bash
# 포맷팅 검증
./gradlew ktlintCheck

# 빌드 및 테스트 (CI와 동일한 명령)
./gradlew build --parallel --build-cache

# 모든 모듈 테스트
./gradlew test
```

## 문제 해결

### 자주 발생하는 이슈

#### 1. JDK 버전 불일치

```bash
# 현재 JDK 버전 확인
java -version

# 필요: Java 25
```

**해결:** [Adoptium Temurin JDK 25](https://adoptium.net/)를 설치하세요.

#### 2. Gradle 빌드 실패

```bash
# Gradle 캐시 삭제
rm -rf ~/.gradle/caches

# 프로젝트 클린 빌드
./gradlew clean build --no-build-cache
```

#### 3. KtLint 포맷팅 오류

```bash
# 포맷팅 자동 적용
./gradlew ktlintFormat

# 특정 파일만 포맷팅
./gradlew ktlintFormat -PktlintFormat=path/to/file.kt
```

#### 4. 테스트 실패

```bash
# 단일 테스트 실행 (디버깅)
./gradlew test --tests "ClubServiceTest" --info

# 테스트 캐시 무시
./gradlew cleanTest test
```

#### 5. Docker 컨테이너 문제

```bash
# 컨테이너 재시작
docker-compose -f compose.local.yaml down
docker-compose -f compose.local.yaml up -d

# 로그 확인
docker-compose -f compose.local.yaml logs -f mysql
docker-compose -f compose.local.yaml logs -f redis
```

#### 6. QueryDSL Q클래스 생성 안 됨

```bash
# KSP 재실행
./gradlew clean kspKotlin
./gradlew build

# Q클래스 위치: build/generated/ksp/main/kotlin/
```

### 데이터베이스 초기화

```bash
# MySQL 컨테이너 재생성
docker-compose -f compose.local.yaml down -v
docker-compose -f compose.local.yaml up -d
```

### 환경 변수 누락

서버 실행 시 환경 변수 오류가 발생하면:

**Unix/macOS**
```bash
# 필수 환경 변수 확인
echo $JWT_SECRET_KEY

# 환경 변수 설정
export JWT_SECRET_KEY="your-secret-key"
```

**Windows (PowerShell)**
```powershell
# 필수 환경 변수 확인
echo $env:JWT_SECRET_KEY

# 환경 변수 설정
$env:JWT_SECRET_KEY="your-secret-key"
```

## 도움이 필요하신가요?

- **이슈 생성**: [GitHub Issues](https://github.com/themoment-team/datagsm-server/issues)
- **이메일**: datagsm.oauth@gmail.com
- **문서**: [README.md](./README.md)

## 라이선스

이 프로젝트는 [MIT 라이선스](./LICENSE)를 따릅니다.

---

다시 한번 기여해주셔서 감사합니다! 🎉
