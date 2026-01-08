import dependency.Dependencies.JJWT
import dependency.Dependencies.JJWT_IMPL
import dependency.Dependencies.JJWT_JACKSON
import dependency.Dependencies.JUNIT_PLATFORM_LAUNCHER
import dependency.Dependencies.KOTEST_ASSERTIONS
import dependency.Dependencies.KOTEST_FRAMEWORK
import dependency.Dependencies.KOTEST_RUNNER
import dependency.Dependencies.KOTLIN_COROUTINES
import dependency.Dependencies.KOTLIN_JUNIT5
import dependency.Dependencies.MOCKK
import dependency.Dependencies.MYSQL_CONNECTOR
import dependency.Dependencies.PEANUT_BUTTER
import dependency.Dependencies.POI
import dependency.Dependencies.POI_OOXML
import dependency.Dependencies.QUERY_DSL
import dependency.Dependencies.SPRING_AOP
import dependency.Dependencies.SPRING_CLOUD_BOM
import dependency.Dependencies.SPRING_DATA_JPA
import dependency.Dependencies.SPRING_DATA_REDIS
import dependency.Dependencies.SPRING_OPENFEIGN
import dependency.Dependencies.SPRING_RETRY
import dependency.Dependencies.SPRING_SECURITY
import dependency.Dependencies.SPRING_SECURITY_TEST
import dependency.Dependencies.SPRINT_MAIL
import dependency.Dependencies.SPRING_TEST
import dependency.Dependencies.SPRING_VALIDATION
import dependency.Dependencies.SPRING_WEB
import dependency.Dependencies.SWAGGER_UI
import dependency.Dependencies.THE_MOMENT_THE_SDK

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "team.themoment"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencyManagement {
    imports {
        mavenBom(SPRING_CLOUD_BOM)
    }
}

dependencies {
    // Common Module
    implementation(project(":datagsm-common"))

    // Spring Boot
    implementation(SPRING_WEB)
    implementation(SPRING_SECURITY)
    implementation(SPRING_VALIDATION)
    implementation(SPRING_DATA_JPA)
    implementation(SPRING_DATA_REDIS)

    // JWT
    implementation(JJWT)
    runtimeOnly(JJWT_IMPL)
    runtimeOnly(JJWT_JACKSON)

    // Apache POI (Excel)
    implementation(POI)
    implementation(POI_OOXML)

    // QueryDSL
    implementation(QUERY_DSL)

    // Database
    runtimeOnly(MYSQL_CONNECTOR)

    // the-sdk
    implementation(THE_MOMENT_THE_SDK)

    // Swagger
    implementation(SWAGGER_UI)

    // Logging
    implementation(PEANUT_BUTTER)

    // Kotlin Coroutines
    implementation(KOTLIN_COROUTINES)

    // Spring AOP & Retry
    implementation(SPRING_AOP)
    implementation(SPRING_RETRY)

    // OpenFeign (for Discord webhook)
    implementation(SPRING_OPENFEIGN)

    // Email
    implementation(SPRINT_MAIL)

    // Testing
    testImplementation(SPRING_TEST)
    testImplementation(KOTLIN_JUNIT5)
    testImplementation(KOTEST_ASSERTIONS)
    testImplementation(KOTEST_RUNNER)
    testImplementation(KOTEST_FRAMEWORK)
    testImplementation(SPRING_SECURITY_TEST)
    testRuntimeOnly(JUNIT_PLATFORM_LAUNCHER)
    testImplementation(MOCKK)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
