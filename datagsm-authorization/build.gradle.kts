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

    // Spring Boot (module-specific)
    implementation(dependency.Dependencies.SPRINT_MAIL)

    // Spring OAuth2
    implementation(dependency.Dependencies.SPRING_OAUTH2_CLIENT)

    // JWT
    implementation(dependency.Dependencies.JJWT)
    runtimeOnly(dependency.Dependencies.JJWT_IMPL)
    runtimeOnly(dependency.Dependencies.JJWT_JACKSON)

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
