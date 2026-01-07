import dependency.Dependencies.BUCKET4J_CORE
import dependency.Dependencies.JACKSON_KOTLIN
import dependency.Dependencies.KOTLIN_COROUTINES
import dependency.Dependencies.KOTLIN_REFLECT
import dependency.Dependencies.MYSQL_CONNECTOR
import dependency.Dependencies.PEANUT_BUTTER
import dependency.Dependencies.SPRING_AOP
import dependency.Dependencies.SPRING_CLOUD_BOM
import dependency.Dependencies.SPRING_DATA_JPA
import dependency.Dependencies.SPRING_DATA_REDIS
import dependency.Dependencies.SPRING_OPENFEIGN
import dependency.Dependencies.SPRING_RETRY
import dependency.Dependencies.SPRING_SECURITY
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
    implementation(SPRING_AOP)

    // Kotlin
    implementation(KOTLIN_REFLECT)
    implementation(KOTLIN_COROUTINES)
    implementation(JACKSON_KOTLIN)

    // OpenFeign
    implementation(SPRING_OPENFEIGN)
    implementation(SPRING_RETRY)

    // Rate Limiting
    implementation(BUCKET4J_CORE)

    // Logging
    implementation(PEANUT_BUTTER)

    // the-sdk
    implementation(THE_MOMENT_THE_SDK)

    // Database
    runtimeOnly(MYSQL_CONNECTOR)

    // Swagger
    implementation(SWAGGER_UI)
}

tasks.bootJar {
    enabled = true
}

tasks.jar {
    enabled = false
}
