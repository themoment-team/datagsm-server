package dependency

import dependency.DependencyVersions.AWS_SDK_VERSION
import dependency.DependencyVersions.BUCKET4J_VERSION
import dependency.DependencyVersions.JACKSON_VERSION
import dependency.DependencyVersions.JJWT_VERSION
import dependency.DependencyVersions.KOTEST_VERSION
import dependency.DependencyVersions.KOTLIN_COROUTINES_VERSION
import dependency.DependencyVersions.MOCKK_VERSION
import dependency.DependencyVersions.PEANUT_BUTTER_VERSION
import dependency.DependencyVersions.POI_VERSION
import dependency.DependencyVersions.QUERY_DSL_JSON_EXTENSION_VERSION
import dependency.DependencyVersions.QUERY_DSL_VERSION
import dependency.DependencyVersions.SPRING_CLOUD_VERSION
import dependency.DependencyVersions.SWAGGER_VERSION
import dependency.DependencyVersions.THE_MOMENT_THE_SDK_VERSION

object Dependencies {
    // Spring Boot
    const val SPRING_WEB = "org.springframework.boot:spring-boot-starter-web"
    const val SPRING_SECURITY = "org.springframework.boot:spring-boot-starter-security"
    const val SPRING_VALIDATION = "org.springframework.boot:spring-boot-starter-validation"
    const val SPRING_AOP = "org.springframework.boot:spring-boot-starter-aop:${DependencyVersions.SPRING_AOP_VERSION}"
    const val SPRINT_MAIL = "org.springframework.boot:spring-boot-starter-mail"

    // Spring Data
    const val SPRING_DATA_JPA = "org.springframework.boot:spring-boot-starter-data-jpa"
    const val SPRING_DATA_REDIS = "org.springframework.boot:spring-boot-starter-data-redis"

    // Spring Cloud
    const val SPRING_OPENFEIGN = "org.springframework.cloud:spring-cloud-starter-openfeign"
    const val SPRING_RETRY = "org.springframework.retry:spring-retry"

    // Spring OAuth2
    const val SPRING_OAUTH2_CLIENT = "org.springframework.boot:spring-boot-starter-oauth2-client"

    // Spring Security
    const val SPRING_SECURITY_CORE = "org.springframework.security:spring-security-core"

    // JWT
    const val JJWT = "io.jsonwebtoken:jjwt-api:${JJWT_VERSION}"
    const val JJWT_IMPL = "io.jsonwebtoken:jjwt-impl:${JJWT_VERSION}"
    const val JJWT_JACKSON = "io.jsonwebtoken:jjwt-jackson:${JJWT_VERSION}"

    // Jakarta EE
    const val JAKARTA_PERSISTENCE_API = "jakarta.persistence:jakarta.persistence-api"
    const val JAKARTA_TRANSACTION_API = "jakarta.transaction:jakarta.transaction-api"
    const val JAKARTA_VALIDATION_API = "jakarta.validation:jakarta.validation-api"

    // Hibernate
    const val HIBERNATE = "org.hibernate.orm:hibernate-core"

    // QueryDSL
    const val QUERY_DSL = "io.github.openfeign.querydsl:querydsl-jpa:${QUERY_DSL_VERSION}"
    const val QUERY_DSL_PROCESSOR = "io.github.openfeign.querydsl:querydsl-ksp-codegen:${QUERY_DSL_VERSION}"
    const val QUERY_DSL_JSON_EXTENSION = "io.github.snowykte0426:querydsl-mysql-json-jpa:${QUERY_DSL_JSON_EXTENSION_VERSION}"

    // Apache POI
    const val POI = "org.apache.poi:poi:${POI_VERSION}"
    const val POI_OOXML = "org.apache.poi:poi-ooxml:${POI_VERSION}"

    // Rate Limiting
    const val BUCKET4J_CORE = "com.bucket4j:bucket4j_jdk17-core:${BUCKET4J_VERSION}"
    const val BUCKET4J_REDIS = "com.bucket4j:bucket4j_jdk17-lettuce:${BUCKET4J_VERSION}"

    // Database
    const val MYSQL_CONNECTOR = "com.mysql:mysql-connector-j"

    // OpenAPI / Swagger
    const val SPRINGDOC_OPENAPI = "org.springdoc:springdoc-openapi-starter-webmvc-ui:${SWAGGER_VERSION}"

    // Custom Libraries
    const val PEANUT_BUTTER = "com.github.snowykte0426:peanut-butter:${PEANUT_BUTTER_VERSION}"
    const val THE_MOMENT_THE_SDK = "com.github.themoment-team:the-sdk:${THE_MOMENT_THE_SDK_VERSION}"

    // Kotlin
    const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect"
    const val KOTLIN_COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${KOTLIN_COROUTINES_VERSION}"
    const val JACKSON_KOTLIN = "tools.jackson.module:jackson-module-kotlin:${JACKSON_VERSION}"

    // Jackson
    const val JACKSON_DATABIND = "tools.jackson.core:jackson-databind"

    // AWS SDK
    const val AWS_CLOUDWATCH_LOGS = "software.amazon.awssdk:cloudwatchlogs"

    // Development Tools
    const val SPRING_BOOT_DEVTOOLS = "org.springframework.boot:spring-boot-devtools"
    const val SPRING_DOCKER_SUPPORT = "org.springframework.boot:spring-boot-docker-compose"

    // Testing
    const val SPRING_TEST = "org.springframework.boot:spring-boot-starter-test"
    const val KOTLIN_JUNIT5 = "org.jetbrains.kotlin:kotlin-test-junit5"
    const val KOTEST_ASSERTIONS = "io.kotest:kotest-assertions-core:${KOTEST_VERSION}"
    const val KOTEST_RUNNER = "io.kotest:kotest-runner-junit5:${KOTEST_VERSION}"
    const val KOTEST_FRAMEWORK = "io.kotest:kotest-framework-engine:${KOTEST_VERSION}"
    const val SPRING_SECURITY_TEST = "org.springframework.security:spring-security-test"
    const val JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher"
    const val MOCKK = "io.mockk:mockk:${MOCKK_VERSION}"

    // BOM
    const val SPRING_BOOT_BOM = "org.springframework.boot:spring-boot-dependencies:${plugin.PluginVersions.SPRING_BOOT_VERSION}"
    const val SPRING_CLOUD_BOM = "org.springframework.cloud:spring-cloud-dependencies:${SPRING_CLOUD_VERSION}"
    const val AWS_SDK_BOM = "software.amazon.awssdk:bom:${AWS_SDK_VERSION}"
}
