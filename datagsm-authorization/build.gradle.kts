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

    // Rate Limiting
    implementation(dependency.Dependencies.BUCKET4J_CORE)
    implementation(dependency.Dependencies.BUCKET4J_REDIS)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
