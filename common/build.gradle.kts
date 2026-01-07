import dependency.Dependencies.JAKARTA_PERSISTENCE_API
import dependency.Dependencies.JAKARTA_TRANSACTION_API
import dependency.Dependencies.JACKSON_KOTLIN
import dependency.Dependencies.KOTLIN_REFLECT
import dependency.Dependencies.SPRING_CLOUD_BOM
import dependency.Dependencies.SPRING_DATA_REDIS
import dependency.Dependencies.QUERY_DSL
import dependency.Dependencies.QUERY_DSL_PROCESSOR

plugins {
    id(plugin.Plugins.KOTLIN_JVM)
    id(plugin.Plugins.KOTLIN_SPRING)
    id(plugin.Plugins.KOTLIN_JPA)
    id(plugin.Plugins.KOTLIN_ALLOPEN)
    id(plugin.Plugins.SPRING_DEPENDENCY_MANAGEMENT) version plugin.PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION
    id(plugin.Plugins.KSP) version plugin.PluginVersions.KSP_VERSION
    idea
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Jakarta EE
    api(JAKARTA_PERSISTENCE_API)
    api(JAKARTA_TRANSACTION_API)

    // Kotlin
    api(JACKSON_KOTLIN)
    api(KOTLIN_REFLECT)

    // Jackson (for StringSetConverter)
    api("com.fasterxml.jackson.core:jackson-databind")

    // Spring Data
    api(SPRING_DATA_REDIS)

    // Spring Security (for GrantedAuthority)
    api("org.springframework.security:spring-security-core")

    // Hibernate
    api("org.hibernate.orm:hibernate-core")

    // QueryDSL
    api(QUERY_DSL)
    ksp(QUERY_DSL_PROCESSOR)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
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

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${plugin.PluginVersions.SPRING_BOOT_VERSION}")
        mavenBom(SPRING_CLOUD_BOM)
    }
}
