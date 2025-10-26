# DataGSM 프로젝트 GitHub Copilot 리뷰 지침

## 프로젝트 개요
DataGSM은 광주소프트웨어마이스터고등학교의 OpenAPI 서비스를 제공하는 Spring Boot 기반의 Kotlin 프로젝트입니다.
학생, 동아리, 급식, 학사일정 등의 학교 정보를 API로 제공하며, Google OAuth2 인증을 통한 API 키 관리 시스템을 구현하고 있습니다.

## 기술 스택 및 환경
- **언어**: Kotlin (Java 24)
- **프레임워크**: Spring Boot 3.x, Spring Security, Spring Data JPA
- **데이터베이스**: MySQL, Redis
- **빌드 도구**: Gradle Kotlin DSL
- **테스트**: Kotest, MockK
- **인증**: JWT, Google OAuth2
- **기타**: QueryDSL, OpenFeign, Swagger UI

## 아키텍처 및 패키지 구조
```
src/main/kotlin/team/themoment/datagsm/
├── domain/                     # 도메인별 모듈
│   ├── auth/                  # 인증 관련 (JWT, OAuth2, API Key)
│   ├── student/               # 학생 정보 관리
│   ├── club/                  # 동아리 정보 관리
│   ├── neis/                  # NEIS 연동 (급식, 학사일정)
│   └── project/               # 프로젝트 정보
└── global/                    # 전역 설정 및 공통 모듈
    ├── config/                # 설정 클래스
    ├── security/              # 보안 관련
    ├── exception/             # 예외 처리
    └── thirdparty/           # 외부 API 연동
```

각 도메인은 다음 구조를 따릅니다:
- `controller/`: REST API 컨트롤러
- `service/`: 비즈니스 로직 (인터페이스 + 구현체)
- `repository/`: 데이터 접근 계층 (JPA + Custom)
- `entity/`: JPA 엔티티 및 상수
- `dto/`: 요청/응답 DTO

## 코드 리뷰 중점 사항

### 1. 보안 검토
- **API 키 및 JWT 토큰 처리**: 민감한 정보가 로그에 노출되지 않는지 확인
- **OAuth2 인증 흐름**: Google OAuth2 연동 시 보안 취약점 검토
- **API 접근 제어**: `@PreAuthorize` 어노테이션과 역할 기반 접근 제어 확인
- **SQL 인젝션 방지**: QueryDSL 사용 시 동적 쿼리의 안전성 검토
- **CORS 설정**: 프로덕션 환경에서 적절한 도메인 제한 확인

### 2. 코드 품질 및 컨벤션
- **Kotlin 코딩 스타일**: Ktlint 규칙 준수 여부 확인
- **네이밍 컨벤션**: 
  - 클래스: PascalCase
  - 함수/변수: camelCase
  - 상수: UPPER_SNAKE_CASE
  - DTO 접미사: ReqDto, ResDto
- **불변성 활용**: `val` 사용 권장, 가변 컬렉션보다 불변 컬렉션 사용
- **널 안전성**: Kotlin의 널 안전성 기능 적극 활용
- **확장 함수**: 적절한 확장 함수 사용으로 코드 가독성 향상

### 3. 아키텍처 및 설계 패턴
- **계층 분리**: Controller-Service-Repository 패턴 준수
- **의존성 주입**: Spring의 생성자 주입 사용 권장
- **서비스 인터페이스**: 모든 서비스는 인터페이스와 구현체로 분리
- **DTO 변환**: Entity와 DTO 간 명확한 분리 및 변환 로직
- **예외 처리**: `ExpectedException`을 상속한 커스텀 예외 사용

### 4. 데이터 액세스 및 성능
- **JPA 성능**: N+1 문제 방지, 적절한 페치 전략 사용
- **QueryDSL**: 복잡한 쿼리는 QueryDSL 활용, Custom Repository 구현
- **트랜잭션**: `@Transactional` 적절한 사용 및 읽기 전용 트랜잭션 활용
- **Redis 캐싱**: 캐시 키 네이밍 규칙 및 TTL 설정 검토

### 5. API 설계
- **RESTful API**: HTTP 메서드와 상태 코드 적절한 사용
- **응답 형식**: `CommonApiResponse` 래퍼 사용 일관성
- **입력 검증**: `@Valid` 어노테이션과 Bean Validation 활용
- **API 문서화**: Swagger 어노테이션 적절한 사용
- **버전 관리**: API 변경 시 하위 호환성 고려

### 6. 테스트 요구사항
- **단위 테스트**: Kotest 프레임워크 사용, Given-When-Then 패턴
- **모킹**: MockK 라이브러리 활용한 의존성 모킹
- **테스트 커버리지**: 새로운 비즈니스 로직에 대한 테스트 필수
- **통합 테스트**: 외부 API 연동 부분에 대한 통합 테스트 고려

### 7. 환경 및 배포
- **프로파일 분리**: application.yaml의 환경별 설정 분리
- **Docker**: stage.dockerfile, prod.dockerfile 적절한 사용
- **CI/CD**: GitHub Actions 워크플로우 검토
- **환경 변수**: 민감한 정보는 환경 변수로 관리

## 금지사항
- 하드코딩된 비밀번호, API 키, 토큰
- System.out.println() 대신 Logger 사용 필수
- 비즈니스 로직을 Controller에 작성
- Entity를 직접 API 응답으로 사용
- 트랜잭션 범위 내에서 외부 API 호출

## 권장사항
- 답변, 리뷰를 진행할때는 한국어로 진행 
- 코드 가독성을 위한 적절한 주석 작성
- 매직 넘버 대신 의미 있는 상수 사용
- 에러 메시지는 사용자 친화적이고 보안상 안전하게
- 로깅 레벨 적절히 사용 (DEBUG, INFO, WARN, ERROR)
- 성능이 중요한 부분은 프로파일링 고려

이 지침을 바탕으로 코드 리뷰 시 위 사항들을 중점적으로 검토하여 DataGSM 프로젝트의 품질과 보안을 향상시켜 주세요.
