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

dependencyManagement {
    imports {
        mavenBom(dependency.Dependencies.SPRING_BOOT_BOM)
        mavenBom(dependency.Dependencies.SPRING_CLOUD_BOM)
        mavenBom(dependency.Dependencies.AWS_SDK_BOM)
    }
}

dependencies {
    // Jakarta EE
    api(dependency.Dependencies.JAKARTA_PERSISTENCE_API)
    api(dependency.Dependencies.JAKARTA_TRANSACTION_API)
    api(dependency.Dependencies.JAKARTA_VALIDATION_API)

    // Spring Boot
    api(dependency.Dependencies.SPRING_WEB)
    api(dependency.Dependencies.SPRING_SECURITY)
    api(dependency.Dependencies.SPRING_VALIDATION)
    api(dependency.Dependencies.SPRING_AOP)
    api(dependency.Dependencies.SPRING_RETRY)

    // Spring Data
    api(dependency.Dependencies.SPRING_DATA_JPA)
    api(dependency.Dependencies.SPRING_DATA_REDIS)

    // Spring Security
    api(dependency.Dependencies.SPRING_SECURITY_CORE)

    // Spring Cloud
    api(dependency.Dependencies.SPRING_OPENFEIGN)

    // Rate Limiting
    api(dependency.Dependencies.BUCKET4J_CORE)
    api(dependency.Dependencies.BUCKET4J_REDIS)

    // Hibernate
    api(dependency.Dependencies.HIBERNATE)

    // QueryDSL
    api(dependency.Dependencies.QUERY_DSL)
    ksp(dependency.Dependencies.QUERY_DSL_PROCESSOR)

    // Database
    api(dependency.Dependencies.MYSQL_CONNECTOR)

    // OpenAPI / Swagger
    api(dependency.Dependencies.SPRINGDOC_OPENAPI)

    // Custom Libraries
    api(dependency.Dependencies.THE_MOMENT_THE_SDK)
    api(dependency.Dependencies.PEANUT_BUTTER)

    // Logging / AWS CloudWatch
    api(dependency.Dependencies.AWS_CLOUDWATCH_LOGS)

    // Development Tools
    api(dependency.Dependencies.SPRING_BOOT_DEVTOOLS)
    api(dependency.Dependencies.SPRING_DOCKER_SUPPORT)

    // Kotlin
    api(dependency.Dependencies.KOTLIN_REFLECT)
    api(dependency.Dependencies.KOTLIN_COROUTINES)
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
