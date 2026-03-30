import com.gorylenko.GitPropertiesPluginExtension
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import test.TestSummaryPlugin

plugins {
    id(plugin.Plugins.KOTLIN_JVM) version plugin.PluginVersions.KOTLIN_VERSION apply false
    id(plugin.Plugins.KOTLIN_SPRING) version plugin.PluginVersions.KOTLIN_VERSION apply false
    id(plugin.Plugins.KOTLIN_JPA) version plugin.PluginVersions.KOTLIN_VERSION apply false
    id(plugin.Plugins.SPRING_BOOT) version plugin.PluginVersions.SPRING_BOOT_VERSION apply false
    id(plugin.Plugins.SPRING_DEPENDENCY_MANAGEMENT) version plugin.PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION apply false
    id(plugin.Plugins.KSP) version plugin.PluginVersions.KSP_VERSION apply false
    id(plugin.Plugins.KTLINT) version plugin.PluginVersions.KTLINT_VERSION apply false
    id(plugin.Plugins.GIT_PROPERTIES) version plugin.PluginVersions.GIT_PROPERTIES_VERSION apply false
}

group = "team.themoment"
version = "v20260330.0"

apply<TestSummaryPlugin>()

subprojects {
    apply(plugin = plugin.Plugins.KOTLIN_JVM)
    apply(plugin = plugin.Plugins.SPRING_DEPENDENCY_MANAGEMENT)
    apply(plugin = plugin.Plugins.KTLINT)

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    extensions.configure<DependencyManagementExtension> {
        imports {
            mavenBom(dependency.Dependencies.SPRING_CLOUD_BOM)
            mavenBom(dependency.Dependencies.AWS_SDK_BOM)
        }
    }

    configure<KtlintExtension> {
        filter {
            exclude("**/build/**")
            exclude {
                projectDir
                    .toURI()
                    .relativize(it.file.toURI())
                    .path
                    .contains("/generated/")
            }
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
