package test

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.services.BuildServiceSpec
import org.gradle.build.event.BuildEventsListenerRegistry
import javax.inject.Inject

class TestSummaryPlugin
    @Inject
    constructor(
        private val registry: BuildEventsListenerRegistry,
    ) : Plugin<Project> {
        override fun apply(target: Project) {
            val service =
                target.gradle.sharedServices.registerIfAbsent(
                    "testSummary",
                    TestSummaryService::class.java,
                    object : Action<BuildServiceSpec<TestSummaryService.Params>> {
                        override fun execute(spec: BuildServiceSpec<TestSummaryService.Params>) {
                            spec.parameters.rootDir.set(target.rootDir)
                        }
                    },
                )
            registry.onTaskCompletion(service)
        }
    }