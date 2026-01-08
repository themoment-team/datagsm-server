plugins {
    id(plugin.Plugins.KOTLIN_JVM) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_SPRING) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_JPA) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.KOTLIN_ALLOPEN) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.SPRING_DEPENDENCY_MANAGEMENT) version plugin.PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION
    id(plugin.Plugins.KSP) version plugin.PluginVersions.KSP_VERSION
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom(dependency.Dependencies.SPRING_BOOT_BOM)
        mavenBom(dependency.Dependencies.AWS_SDK_BOM)
    }
}

dependencies {
    // Jakarta EE
    api(dependency.Dependencies.JAKARTA_PERSISTENCE_API)
    api(dependency.Dependencies.JAKARTA_TRANSACTION_API)

    // Spring Data
    api(dependency.Dependencies.SPRING_DATA_JPA)
    api(dependency.Dependencies.SPRING_DATA_REDIS)

    // Spring Security
    api(dependency.Dependencies.SPRING_SECURITY_CORE)

    // Hibernate
    api(dependency.Dependencies.HIBERNATE)

    // QueryDSL
    api(dependency.Dependencies.QUERY_DSL)
    ksp(dependency.Dependencies.QUERY_DSL_PROCESSOR)

    // Kotlin
    api(dependency.Dependencies.KOTLIN_REFLECT)
    api(dependency.Dependencies.JACKSON_KOTLIN)

    // Jackson
    api(dependency.Dependencies.JACKSON_DATABIND)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }

    sourceSets.main {
        kotlin.srcDirs("build/generated/ksp/main/kotlin")
    }
}
