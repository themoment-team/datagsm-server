plugins {
    id(plugin.Plugins.KOTLIN_JVM) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_SPRING) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_JPA) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.SPRING_BOOT) version plugin.PluginVersions.SPRING_BOOT_VERSION
    id(plugin.Plugins.SPRING_DEPENDENCY_MANAGEMENT) version plugin.PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION
}

group = "team.themoment"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencyManagement {
    imports {
        mavenBom(dependency.Dependencies.SPRING_CLOUD_BOM)
    }
}

dependencies {
    // Common Module
    implementation(project(":datagsm-common"))

    // Spring Boot
    implementation(dependency.Dependencies.SPRING_WEB)
    implementation(dependency.Dependencies.SPRING_SECURITY)
    implementation(dependency.Dependencies.SPRING_VALIDATION)
    implementation(dependency.Dependencies.SPRING_AOP)
    implementation(dependency.Dependencies.SPRING_RETRY)
    implementation(dependency.Dependencies.SPRINT_MAIL)

    // Spring Data
    implementation(dependency.Dependencies.SPRING_DATA_JPA)
    implementation(dependency.Dependencies.SPRING_DATA_REDIS)

    // Spring Cloud
    implementation(dependency.Dependencies.SPRING_OPENFEIGN)

    // Spring OAuth2
    implementation(dependency.Dependencies.SPRING_OAUTH2_CLIENT)

    // JWT
    implementation(dependency.Dependencies.JJWT)
    runtimeOnly(dependency.Dependencies.JJWT_IMPL)
    runtimeOnly(dependency.Dependencies.JJWT_JACKSON)

    // Rate Limiting
    implementation(dependency.Dependencies.BUCKET4J_CORE)
    implementation(dependency.Dependencies.BUCKET4J_REDIS)

    // Database
    runtimeOnly(dependency.Dependencies.MYSQL_CONNECTOR)

    // Swagger
    implementation(dependency.Dependencies.SWAGGER_UI)

    // Custom Libraries
    implementation(dependency.Dependencies.THE_MOMENT_THE_SDK)

    // Logging
    implementation(dependency.Dependencies.LOGBACK_AWS_APPENDER)

    // Development Tools
    developmentOnly(dependency.Dependencies.SPRING_BOOT_DEVTOOLS)
    developmentOnly(dependency.Dependencies.SPRING_DOCKER_SUPPORT)

    // Kotlin
    implementation(dependency.Dependencies.KOTLIN_REFLECT)
    implementation(dependency.Dependencies.KOTLIN_COROUTINES)
    implementation(dependency.Dependencies.JACKSON_KOTLIN)

    // Testing
    testImplementation(dependency.Dependencies.SPRING_TEST)
    testImplementation(dependency.Dependencies.KOTLIN_JUNIT5)
    testImplementation(dependency.Dependencies.KOTEST_ASSERTIONS)
    testImplementation(dependency.Dependencies.KOTEST_RUNNER)
    testImplementation(dependency.Dependencies.KOTEST_FRAMEWORK)
    testImplementation(dependency.Dependencies.SPRING_SECURITY_TEST)
    testRuntimeOnly(dependency.Dependencies.JUNIT_PLATFORM_LAUNCHER)
    testImplementation(dependency.Dependencies.MOCKK)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
