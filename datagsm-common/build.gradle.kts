plugins {
    id(plugin.Plugins.KOTLIN_SPRING)
    id(plugin.Plugins.KOTLIN_JPA)
    id(plugin.Plugins.KSP)
}

dependencyManagement {
    imports {
        mavenBom(dependency.Dependencies.SPRING_BOOT_BOM)
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
    api(dependency.Dependencies.SPRING_ASPECTJ)
    api(dependency.Dependencies.SPRING_RETRY)
    api(dependency.Dependencies.SPRING_THYMELEAF)

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

    // Stage profile: embedded infrastructure (no external Redis/MySQL needed)
    runtimeOnly(dependency.Dependencies.H2)
    implementation(dependency.Dependencies.EMBEDDED_REDIS)

    // Custom Libraries
    api(dependency.Dependencies.THE_MOMENT_THE_SDK)
    api(dependency.Dependencies.QUERY_DSL_JSON_EXTENSION)

    // Logging / AWS CloudWatch
    api(dependency.Dependencies.AWS_CLOUDWATCH_LOGS)

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
