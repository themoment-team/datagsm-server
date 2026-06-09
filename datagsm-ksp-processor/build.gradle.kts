import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "team.themoment"

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:${plugin.PluginVersions.KSP_VERSION}")
    implementation(dependency.Dependencies.KOTLIN_POET)
    implementation(dependency.Dependencies.KOTLIN_POET_KSP)

    testImplementation(dependency.Dependencies.KOTEST_ASSERTIONS)
    testImplementation(dependency.Dependencies.KOTEST_RUNNER)
    testImplementation(dependency.Dependencies.KOTEST_FRAMEWORK)
    testRuntimeOnly(dependency.Dependencies.JUNIT_PLATFORM_LAUNCHER)
}

// A transitive constraint pulls kotlin-reflect up to 2.3.20 while stdlib stays at the project
// version, so reflect fails at test startup with NoClassDefFoundError. Pin reflect to stdlib.
configurations.matching { it.name.startsWith("test") }.configureEach {
    resolutionStrategy.force("org.jetbrains.kotlin:kotlin-reflect:${plugin.PluginVersions.KOTLIN_VERSION}")
}

tasks.test {
    useJUnitPlatform()
}
