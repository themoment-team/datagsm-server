@file:Suppress("UnstableApiUsage")

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.build.event.BuildEventsListenerRegistry
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.gradle.tooling.events.task.TaskFinishEvent
import org.gradle.tooling.events.task.TaskSkippedResult
import javax.xml.parsers.DocumentBuilderFactory

abstract class TestSummaryService :
    BuildService<TestSummaryService.Params>,
    OperationCompletionListener {
    interface Params : BuildServiceParameters {
        val rootDir: DirectoryProperty
    }

    override fun onFinish(event: FinishEvent) {
        if (event !is TaskFinishEvent) return
        val segments = event.descriptor.taskPath.split(":")
        if (segments.lastOrNull() != "test") return
        val moduleName = segments.dropLast(1).lastOrNull()?.takeIf { it.isNotEmpty() } ?: return

        val resultsDir =
            parameters.rootDir
                .get()
                .dir("$moduleName/build/test-results/test")
                .asFile
        if (!resultsDir.exists()) return

        var total = 0
        var failures = 0
        var errors = 0
        var skipped = 0

        resultsDir
            .walkTopDown()
            .filter { it.isFile && it.extension == "xml" }
            .forEach { file ->
                runCatching {
                    val root =
                        DocumentBuilderFactory
                            .newInstance()
                            .newDocumentBuilder()
                            .parse(file)
                            .documentElement
                    total += root.getAttribute("tests").toIntOrNull() ?: 0
                    failures += root.getAttribute("failures").toIntOrNull() ?: 0
                    errors += root.getAttribute("errors").toIntOrNull() ?: 0
                    skipped += root.getAttribute("skipped").toIntOrNull() ?: 0
                }
            }

        if (total == 0) return
        val failed = failures + errors
        val passed = total - failed - skipped
        val cached = if (event.result is TaskSkippedResult) " (cached)" else ""
        println("\n[$moduleName]$cached Total $total | Pass $passed | Skip $skipped | Fail $failed")
    }
}

plugins {
    id(plugin.Plugins.KOTLIN_JVM) version plugin.PluginVersions.KOTLIN_VERSION apply false
    id(plugin.Plugins.KOTLIN_SPRING) version plugin.PluginVersions.KOTLIN_VERSION apply false
    id(plugin.Plugins.KOTLIN_JPA) version plugin.PluginVersions.KOTLIN_VERSION apply false
    id(plugin.Plugins.SPRING_BOOT) version plugin.PluginVersions.SPRING_BOOT_VERSION apply false
    id(plugin.Plugins.SPRING_DEPENDENCY_MANAGEMENT) version plugin.PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION apply false
    id(plugin.Plugins.KSP) version plugin.PluginVersions.KSP_VERSION apply false
    id(plugin.Plugins.KTLINT) version plugin.PluginVersions.KTLINT_VERSION apply false
}

group = "team.themoment"
version = "v20260220.0"

val testSummaryService =
    gradle.sharedServices.registerIfAbsent("testSummary", TestSummaryService::class) {
        parameters.rootDir.set(rootDir)
    }
serviceOf<BuildEventsListenerRegistry>().onTaskCompletion(testSummaryService)

subprojects {
    apply(plugin = plugin.Plugins.KOTLIN_JVM)
    apply(plugin = plugin.Plugins.SPRING_DEPENDENCY_MANAGEMENT)
    apply(plugin = plugin.Plugins.KTLINT)

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    extensions.configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom(dependency.Dependencies.SPRING_CLOUD_BOM)
            mavenBom(dependency.Dependencies.AWS_SDK_BOM)
        }
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
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
}
