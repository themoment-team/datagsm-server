package plugin

object Plugins {
    const val SPRING_BOOT = "org.springframework.boot"
    const val SPRING_CLOUD_DEPENDENCY_MANAGEMENT = "org.springframework.cloud:spring-cloud-dependencies:${PluginVersions.SPRING_CLOUD_DEPENDENCY_MANAGEMENT}"
    const val SPRING_DEPENDENCY_MANAGEMENT = "io.spring.dependency-management"
    const val KOTLIN_JVM = "org.jetbrains.kotlin.jvm"
    const val KOTLIN_SPRING = "org.jetbrains.kotlin.plugin.spring"
    const val KOTLIN_JPA = "org.jetbrains.kotlin.plugin.jpa"
    const val KOTLIN_ALLOPEN = "org.jetbrains.kotlin.plugin.allopen"
    const val KSP = "com.google.devtools.ksp"
    const val KOTEST = "io.kotest"
    const val KTLINT = "org.jlleitschuh.gradle.ktlint"
}
