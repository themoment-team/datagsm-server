package dependency

object Dependencies {
    /* Spring Starters */
    const val SPRING_WEB = "org.springframework.boot:spring-boot-starter-web"
    const val SPRING_VALIDATION = "org.springframework.boot:spring-boot-starter-validation"
    const val SPRING_SECURITY = "org.springframework.boot:spring-boot-starter-security"
    const val SPRINT_MAIL = "org.springframework.boot:spring-boot-starter-mail"
    const val SPRING_OPENFEIGN = "org.springframework.cloud:spring-cloud-starter-openfeign"

    /* JWT */
    const val JJWT = "io.jsonwebtoken:jjwt-api:${DependencyVersions.JJWT_VERSION}"
    const val JJWT_IMPL = "io.jsonwebtoken:jjwt-impl:${DependencyVersions.JJWT_VERSION}"
    const val JJWT_JACKSON = "io.jsonwebtoken:jjwt-jackson:${DependencyVersions.JJWT_VERSION}"

    /* QueryDSL */
    const val QUERY_DSL = "io.github.openfeign.querydsl:querydsl-jpa:${DependencyVersions.QUERY_DSL_VERSION}"
    const val QUERY_DSL_PROCESSOR = "io.github.openfeign.querydsl:querydsl-ksp-codegen:${DependencyVersions.QUERY_DSL_VERSION}"

    /* Jakarta EE */
    const val JAKARTA_PERSISTENCE_API = "jakarta.persistence:jakarta.persistence-api"
    const val JAKARTA_TRANSACTION_API = "jakarta.transaction:jakarta.transaction-api"

    /* Spring Data */
    const val SPRING_DATA_JPA = "org.springframework.boot:spring-boot-starter-data-jpa"
    const val SPRING_DATA_REDIS = "org.springframework.boot:spring-boot-starter-data-redis"

    /* Development Tools */
    const val SPRING_DOCKER_SUPPORT = "org.springframework.boot:spring-boot-docker-compose"

    /* Kotlin */
    const val JACKSON_KOTLIN = "com.fasterxml.jackson.module:jackson-module-kotlin"
    const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect"

    /* Database */
    const val MYSQL_CONNECTOR = "com.mysql:mysql-connector-j"

    /* Swagger */
    const val SWAGGER_UI = "org.springdoc:springdoc-openapi-starter-webmvc-ui:${DependencyVersions.SWAGGER_VERSION}"

    /* Testing */
    const val SPRING_TEST = "org.springframework.boot:spring-boot-starter-test"
    const val KOTLIN_JUNIT5 = "org.jetbrains.kotlin:kotlin-test-junit5"
    const val KOTEST = "io.kotest:kotest-assertions-core:${DependencyVersions.KOTEST_VERSION}"
    const val SPRING_SECURITY_TEST = "org.springframework.security:spring-security-test"
    const val JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher"
    const val MOCKK = "io.mockk:mockk:${DependencyVersions.MOCKK_VERSION}"
}
