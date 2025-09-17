plugins {
    id(plugin.Plugins.SPRING_BOOT) version plugin.PluginVersions.SPRING_BOOT_VERSION
    id(plugin.Plugins.DEPENDENCY_MANAGEMENT) version plugin.PluginVersions.DEPENDENCY_MANAGEMENT_VERSION
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
    implementation(dependency.Dependencies.SPRING_WEB)
    implementation(dependency.Dependencies.SPRING_VALIDATION)
    implementation(dependency.Dependencies.SPRING_SECURITY)
    implementation(dependency.Dependencies.SPRINT_MAIL)

    // JWT
    implementation(dependency.Dependencies.JJWT)
    runtimeOnly(dependency.Dependencies.JJWT_IMPL)
    runtimeOnly(dependency.Dependencies.JJWT_JACKSON)

    // QueryDSL
    implementation(dependency.Dependencies.QUERY_DSL)

    // Jakarta EE
    implementation(dependency.Dependencies.JAKARTA_PERSISTENCE_API)
    implementation(dependency.Dependencies.JAKARTA_TRANSACTION_API)

    // Spring Data
    implementation(dependency.Dependencies.SPRING_DATA_JPA)
    implementation(dependency.Dependencies.SPRING_DATA_REDIS)

    // Development Tools
    developmentOnly(dependency.Dependencies.SPRING_DOCKER_SUPPORT)

    // Kotlin
    implementation(dependency.Dependencies.JACKSON_KOTLIN)
    implementation(dependency.Dependencies.KOTLIN_REFLECT)

    // Database
    runtimeOnly(dependency.Dependencies.MYSQL_CONNECTOR)

    // Swagger
    implementation(dependency.Dependencies.SWAGGER_UI)

    // Testing
    testImplementation(dependency.Dependencies.SPRING_TEST)
    testImplementation(dependency.Dependencies.KOTLIN_JUNIT5)
    testImplementation(dependency.Dependencies.KOTEST)
    testImplementation(dependency.Dependencies.SPRING_SECURITY_TEST)
    testRuntimeOnly(dependency.Dependencies.JUNIT_PLATFORM_LAUNCHER)
    testImplementation(dependency.Dependencies.MOCKK)
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

ksp {
    arg("querydsl.entityAccessors", "true")
    arg("querydsl.useFields", "false")
    arg("querydsl.kotlin", "true")
    arg("querydsl.nullCheck", "true")
    arg("querydsl.packageMapping", "true")
}

kotlin {
    sourceSets.main {
        kotlin.srcDirs("build/generated/ksp/main/kotlin")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("kspKotlin")
}
