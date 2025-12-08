package dependency

import dependency.DependencyVersions.AWS_SDK_VERSION
import dependency.DependencyVersions.JJWT_VERSION
import dependency.DependencyVersions.KOTEST_VERSION
import dependency.DependencyVersions.KOTLIN_COROUTINES_VERSION
import dependency.DependencyVersions.LOGBACK_AWS_APPENDER_VERSION
import dependency.DependencyVersions.MOCKK_VERSION
import dependency.DependencyVersions.PEANUT_BUTTER_VERSION
import dependency.DependencyVersions.QUERY_DSL_VERSION
import dependency.DependencyVersions.SPRING_CLOUD_VERSION
import dependency.DependencyVersions.SWAGGER_VERSION

object Dependencies {
    // Spring Starters
    const val SPRING_WEB = "org.springframework.boot:spring-boot-starter-web"
    const val SPRING_VALIDATION = "org.springframework.boot:spring-boot-starter-validation"
    const val SPRING_SECURITY = "org.springframework.boot:spring-boot-starter-security"
    const val SPRING_OAUTH2_CLIENT = "org.springframework.boot:spring-boot-starter-oauth2-client"
    const val SPRINT_MAIL = "org.springframework.boot:spring-boot-starter-mail"
    const val SPRING_OPENFEIGN = "org.springframework.cloud:spring-cloud-starter-openfeign"

    // JWT
    const val JJWT = "io.jsonwebtoken:jjwt-api:${JJWT_VERSION}"
    const val JJWT_IMPL = "io.jsonwebtoken:jjwt-impl:${JJWT_VERSION}"
    const val JJWT_JACKSON = "io.jsonwebtoken:jjwt-jackson:${JJWT_VERSION}"

    // QueryDSL
    const val QUERY_DSL = "io.github.openfeign.querydsl:querydsl-jpa:${QUERY_DSL_VERSION}"
    const val QUERY_DSL_PROCESSOR = "io.github.openfeign.querydsl:querydsl-ksp-codegen:${QUERY_DSL_VERSION}"

    // Jakarta EE
    const val JAKARTA_PERSISTENCE_API = "jakarta.persistence:jakarta.persistence-api"
    const val JAKARTA_TRANSACTION_API = "jakarta.transaction:jakarta.transaction-api"

    // Spring Data
    const val SPRING_DATA_JPA = "org.springframework.boot:spring-boot-starter-data-jpa"
    const val SPRING_DATA_REDIS = "org.springframework.boot:spring-boot-starter-data-redis"

    // Development Tools
    const val SPRING_DOCKER_SUPPORT = "org.springframework.boot:spring-boot-docker-compose"

    // Kotlin
    const val JACKSON_KOTLIN = "com.fasterxml.jackson.module:jackson-module-kotlin"
    const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect"
    const val KOTLIN_COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${KOTLIN_COROUTINES_VERSION}"

    // Database
    const val MYSQL_CONNECTOR = "com.mysql:mysql-connector-j"

    // Swagger
    const val SWAGGER_UI = "org.springdoc:springdoc-openapi-starter-webmvc-ui:${SWAGGER_VERSION}"

    // Logging
    const val LOGBACK_AWS_APPENDER = "ca.pjer:logback-awslogs-appender:${LOGBACK_AWS_APPENDER_VERSION}"

    // BOM
    const val SPRING_CLOUD_BOM = "org.springframework.cloud:spring-cloud-dependencies:${SPRING_CLOUD_VERSION}"
    const val AWS_SDK_BOM = "software.amazon.awssdk:bom:${AWS_SDK_VERSION}"

    // Excel
    const val POI = "org.apache.poi:poi:5.5.1"
    const val POI_OOXML = "org.apache.poi:poi-ooxml:5.5.1"

    // Testing
    const val SPRING_TEST = "org.springframework.boot:spring-boot-starter-test"
    const val KOTLIN_JUNIT5 = "org.jetbrains.kotlin:kotlin-test-junit5"
    const val KOTEST_ASSERTIONS = "io.kotest:kotest-assertions-core:${KOTEST_VERSION}"
    const val KOTEST_RUNNER = "io.kotest:kotest-runner-junit5:${KOTEST_VERSION}"
    const val KOTEST_FRAMEWORK = "io.kotest:kotest-framework-engine:${KOTEST_VERSION}"
    const val SPRING_SECURITY_TEST = "org.springframework.security:spring-security-test"
    const val JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher"
    const val MOCKK = "io.mockk:mockk:${MOCKK_VERSION}"

    // Custom Libraries
    const val PEANUT_BUTTER = "com.github.snowykte0426:peanut-butter:${PEANUT_BUTTER_VERSION}"
}
