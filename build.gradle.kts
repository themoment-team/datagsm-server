import com.gorylenko.GitPropertiesPluginExtension
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import test.TestSummaryPlugin

plugins {
    id(plugin.Plugins.KOTLIN_JVM) version plugin.PluginVersions.KOTLIN_VERSION apply false
    id(plugin.Plugins.KOTLIN_SPRING) version plugin.PluginVersions.KOTLIN_VERSION apply false
    id(plugin.Plugins.KOTLIN_JPA) version plugin.PluginVersions.KOTLIN_VERSION apply false
    id(plugin.Plugins.KOTLIN_MULTIPLATFORM) version plugin.PluginVersions.KOTLIN_VERSION apply false
    id(plugin.Plugins.KOTLIN_SERIALIZATION) version plugin.PluginVersions.KOTLIN_VERSION apply false
    id(plugin.Plugins.SPRING_BOOT) version plugin.PluginVersions.SPRING_BOOT_VERSION apply false
    id(plugin.Plugins.SPRING_DEPENDENCY_MANAGEMENT) version plugin.PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION apply false
    id(plugin.Plugins.KSP) version plugin.PluginVersions.KSP_VERSION apply false
    id(plugin.Plugins.KTLINT) version plugin.PluginVersions.KTLINT_VERSION apply false
    id(plugin.Plugins.GIT_PROPERTIES) version plugin.PluginVersions.GIT_PROPERTIES_VERSION apply false
}

group = "team.themoment"
version = "v20260529.0"

apply<TestSummaryPlugin>()

// Spring Boot service modules sharing the same plugin set, dependencies, and task config.
// Kept here (rather than a buildSrc precompiled script plugin) because such a plugin makes
// ktlint pick up KSP-generated sources, breaking the generated-source exclusion below.
val serviceModules =
    setOf(
        "datagsm-web",
        "datagsm-oauth-authorization",
        "datagsm-openapi",
        "datagsm-oauth-userinfo",
    )

subprojects {
    val isKmpModule = name == "datagsm-shared"
    val isServiceModule = name in serviceModules

    if (!isKmpModule) {
        apply(plugin = plugin.Plugins.KOTLIN_JVM)
        apply(plugin = plugin.Plugins.SPRING_DEPENDENCY_MANAGEMENT)
    }
    apply(plugin = plugin.Plugins.KTLINT)

    if (!isKmpModule) {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion = JavaLanguageVersion.of(25)
            }
        }
    }

    if (!isKmpModule) {
        extensions.configure<DependencyManagementExtension> {
            imports {
                mavenBom(dependency.Dependencies.SPRING_CLOUD_BOM)
                mavenBom(dependency.Dependencies.AWS_SDK_BOM)
            }
        }
    }

    if (isServiceModule) {
        apply(plugin = plugin.Plugins.KOTLIN_SPRING)
        apply(plugin = plugin.Plugins.KOTLIN_JPA)
        apply(plugin = plugin.Plugins.SPRING_BOOT)
        apply(plugin = plugin.Plugins.GIT_PROPERTIES)

        dependencies {
            "implementation"(project(":datagsm-common"))
            "implementation"(dependency.Dependencies.DISENDER_SPRING_BOOT_STARTER)

            "developmentOnly"(dependency.Dependencies.SPRING_BOOT_DEVTOOLS)
            "developmentOnly"(dependency.Dependencies.SPRING_DOCKER_SUPPORT)

            "testImplementation"(dependency.Dependencies.SPRING_TEST)
            "testImplementation"(dependency.Dependencies.KOTLIN_JUNIT5)
            "testImplementation"(dependency.Dependencies.KOTEST_ASSERTIONS)
            "testImplementation"(dependency.Dependencies.KOTEST_RUNNER)
            "testImplementation"(dependency.Dependencies.KOTEST_FRAMEWORK)
            "testImplementation"(dependency.Dependencies.SPRING_SECURITY_TEST)
            "testRuntimeOnly"(dependency.Dependencies.JUNIT_PLATFORM_LAUNCHER)
            "testImplementation"(dependency.Dependencies.MOCKK)
        }

        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions {
                freeCompilerArgs.add("-Xjsr305=strict")
            }
        }

        tasks.withType<Test> {
            useJUnitPlatform()
        }

        tasks.named<Jar>("jar") {
            enabled = false
        }
    }

    configure<KtlintExtension> {
        filter {
            // Stateless lambda (captures no projectDir/Project) so the filter is
            // serializable for the configuration cache. KSP registers build/generated as a
            // separate source root, so relativePath has no "build" segment — match the
            // absolute path instead. Scoped to "/build/generated/" to avoid false positives
            // from a checkout directory that merely contains "/build/" in its path.
            exclude { it.file.invariantSeparatorsPath.contains("/build/generated/") }
        }
    }

    configurations.all {
        exclude(group = "org.bouncycastle")
    }

    plugins.withId(plugin.Plugins.GIT_PROPERTIES) {
        configure<GitPropertiesPluginExtension> {
            dotGitDirectory = rootProject.file(".git")
            keys =
                listOf(
                    "git.branch",
                    "git.commit.id.abbrev",
                    "git.commit.time",
                    "git.commit.message.short",
                )
        }
    }
}
