plugins {
    id(plugin.Plugins.KOTLIN_SPRING)
    id(plugin.Plugins.KOTLIN_JPA)
    id(plugin.Plugins.SPRING_BOOT)
    id(plugin.Plugins.GIT_PROPERTIES)
}

dependencies {
    // Common Module
    implementation(project(":datagsm-common"))

    // Development Tools
    developmentOnly(dependency.Dependencies.SPRING_BOOT_DEVTOOLS)
    developmentOnly(dependency.Dependencies.SPRING_DOCKER_SUPPORT)

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

tasks.jar {
    enabled = false
}
