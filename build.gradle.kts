import dependency.Dependencies.AWS_SDK_BOM
import dependency.Dependencies.JACKSON_KOTLIN
import dependency.Dependencies.JAKARTA_PERSISTENCE_API
import dependency.Dependencies.JAKARTA_TRANSACTION_API
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
import dependency.Dependencies.LOGBACK_AWS_APPENDER
import dependency.Dependencies.MOCKK
import dependency.Dependencies.MYSQL_CONNECTOR
import dependency.Dependencies.PEANUT_BUTTER
import dependency.Dependencies.POI
import dependency.Dependencies.QUERY_DSL
import dependency.Dependencies.QUERY_DSL_PROCESSOR
import dependency.Dependencies.SPRING_CLOUD_BOM
import dependency.Dependencies.SPRING_DATA_JPA
import dependency.Dependencies.SPRING_DATA_REDIS
import dependency.Dependencies.SPRING_DOCKER_SUPPORT
import dependency.Dependencies.SPRING_OAUTH2_CLIENT
import dependency.Dependencies.SPRING_OPENFEIGN
import dependency.Dependencies.SPRING_SECURITY
import dependency.Dependencies.SPRING_SECURITY_TEST
import dependency.Dependencies.SPRING_TEST
import dependency.Dependencies.SPRING_VALIDATION
import dependency.Dependencies.SPRING_WEB
import dependency.Dependencies.SPRINT_MAIL
import dependency.Dependencies.SWAGGER_UI
import dependency.Dependencies.OOXML

plugins {
    id(plugin.Plugins.SPRING_BOOT) version plugin.PluginVersions.SPRING_BOOT_VERSION
    id(plugin.Plugins.SPRING_DEPENDENCY_MANAGEMENT) version plugin.PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION
    id(plugin.Plugins.KSP) version plugin.PluginVersions.KSP_VERSION
    id(plugin.Plugins.KOTLIN_JVM) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_SPRING) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_JPA) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_ALLOPEN) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTEST) version plugin.PluginVersions.KOTEST_VERSION
    id(plugin.Plugins.KTLINT) version plugin.PluginVersions.KTLINT_VERSION
    idea
}

group = "team.themoment"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_24

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
    // Spring Starters
    implementation(SPRING_WEB)
    implementation(SPRING_VALIDATION)
    implementation(SPRING_SECURITY)
    implementation(SPRING_OAUTH2_CLIENT)
    implementation(SPRINT_MAIL)
    implementation(SPRING_OPENFEIGN)

    // JWT
    implementation(JJWT)
    runtimeOnly(JJWT_IMPL)
    runtimeOnly(JJWT_JACKSON)

    // QueryDSL
    implementation(QUERY_DSL)
    ksp(QUERY_DSL_PROCESSOR)

    // Jakarta EE
    implementation(JAKARTA_PERSISTENCE_API)
    implementation(JAKARTA_TRANSACTION_API)

    // Spring Data
    implementation(SPRING_DATA_JPA)
    implementation(SPRING_DATA_REDIS)

    // Development Tools
    developmentOnly(SPRING_DOCKER_SUPPORT)

    // Kotlin
    implementation(JACKSON_KOTLIN)
    implementation(KOTLIN_REFLECT)
    implementation(KOTLIN_COROUTINES)

    // Database
    runtimeOnly(MYSQL_CONNECTOR)

    // Swagger
    implementation(SWAGGER_UI)

    // Logging
    implementation(LOGBACK_AWS_APPENDER)

    // Excel
    implementation(POI)
    implementation(OOXML)

    // Testing
    testImplementation(SPRING_TEST)
    testImplementation(KOTLIN_JUNIT5)
    testImplementation(KOTEST_ASSERTIONS)
    testImplementation(KOTEST_RUNNER)
    testImplementation(KOTEST_FRAMEWORK)
    testImplementation(SPRING_SECURITY_TEST)
    testRuntimeOnly(JUNIT_PLATFORM_LAUNCHER)
    testImplementation(MOCKK)

    // Custom Libraries
    implementation(PEANUT_BUTTER)
}
kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

idea {
    module {
        val kspMain = file("build/generated/ksp/main/kotlin")
        sourceDirs.add(kspMain)
        generatedSourceDirs.add(kspMain)
    }
}

kotlin {
    sourceSets.main {
        kotlin.srcDirs("build/generated/ksp/main/kotlin")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("kspKotlin")
}

ktlint {
    filter {
        exclude("**/build/**")
        exclude {
            projectDir
                .toURI()
                .relativize(it.file.toURI())
                .path
                .contains("/generated/")
        }
    }
}

dependencyManagement {
    imports {
        mavenBom(SPRING_CLOUD_BOM)
        mavenBom(AWS_SDK_BOM)
    }
}
