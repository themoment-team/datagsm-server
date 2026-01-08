import dependency.Dependencies.BUCKET4J_CORE
import dependency.Dependencies.BUCKET4J_REDIS
import dependency.Dependencies.JACKSON_KOTLIN
import dependency.Dependencies.JJWT
import dependency.Dependencies.JJWT_IMPL
import dependency.Dependencies.JJWT_JACKSON
import dependency.Dependencies.JUNIT_PLATFORM_LAUNCHER
import dependency.Dependencies.KOTEST_ASSERTIONS
import dependency.Dependencies.KOTEST_FRAMEWORK
import dependency.Dependencies.KOTEST_RUNNER
import dependency.Dependencies.KOTLIN_COROUTINES
import dependency.Dependencies.KOTLIN_JUNIT5
import dependency.Dependencies.KOTLIN_REFLECT
import dependency.Dependencies.MOCKK
import dependency.Dependencies.MYSQL_CONNECTOR
import dependency.Dependencies.SPRING_AOP
import dependency.Dependencies.SPRING_CLOUD_BOM
import dependency.Dependencies.SPRING_DATA_JPA
import dependency.Dependencies.SPRING_DATA_REDIS
import dependency.Dependencies.SPRING_OAUTH2_CLIENT
import dependency.Dependencies.SPRING_OPENFEIGN
import dependency.Dependencies.SPRING_RETRY
import dependency.Dependencies.SPRING_SECURITY
import dependency.Dependencies.SPRING_SECURITY_TEST
import dependency.Dependencies.SPRING_TEST
import dependency.Dependencies.SPRING_VALIDATION
import dependency.Dependencies.SPRING_WEB
import dependency.Dependencies.SPRINT_MAIL
import dependency.Dependencies.SWAGGER_UI
import dependency.Dependencies.THE_MOMENT_THE_SDK

plugins {
    id(plugin.Plugins.SPRING_BOOT) version plugin.PluginVersions.SPRING_BOOT_VERSION
    id(plugin.Plugins.SPRING_DEPENDENCY_MANAGEMENT) version plugin.PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION
    id(plugin.Plugins.KOTLIN_JVM)
    id(plugin.Plugins.KOTLIN_SPRING)
    id(plugin.Plugins.KOTLIN_JPA)
    id(plugin.Plugins.KOTLIN_ALLOPEN)
    idea
}

group = "team.themoment"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // Common Module
    implementation(project(":datagsm-common"))

    // Spring Starters
    implementation(SPRING_WEB)
    implementation(SPRING_VALIDATION)
    implementation(SPRING_SECURITY)
    implementation(SPRING_OAUTH2_CLIENT)
    implementation(SPRINT_MAIL)
    implementation(SPRING_OPENFEIGN)
    implementation(SPRING_RETRY)
    implementation(SPRING_AOP)

    // Spring Data
    implementation(SPRING_DATA_JPA)
    implementation(SPRING_DATA_REDIS)

    // JWT
    implementation(JJWT)
    runtimeOnly(JJWT_IMPL)
    runtimeOnly(JJWT_JACKSON)

    // Kotlin
    implementation(JACKSON_KOTLIN)
    implementation(KOTLIN_REFLECT)
    implementation(KOTLIN_COROUTINES)

    // Database
    runtimeOnly(MYSQL_CONNECTOR)

    // Swagger
    implementation(SWAGGER_UI)

    // Rate Limiting (Email)
    implementation(BUCKET4J_CORE)
    implementation(BUCKET4J_REDIS)

    // the-sdk
    implementation(THE_MOMENT_THE_SDK)

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

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencyManagement {
    imports {
        mavenBom(SPRING_CLOUD_BOM)
    }
}
