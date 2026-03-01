import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logging
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.gradle.tooling.events.task.TaskFinishEvent
import javax.xml.parsers.DocumentBuilderFactory

abstract class TestSummaryService :
    BuildService<TestSummaryService.Params>,
    OperationCompletionListener {
    interface Params : BuildServiceParameters {
        val rootDir: DirectoryProperty
    }

    private val logger = Logging.getLogger(TestSummaryService::class.java)

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
        val summary =
            buildList {
                add("$total tests completed")
                if (failed > 0) add("$failed failed")
                if (skipped > 0) add("$skipped skipped")
            }.joinToString(", ")
        val taskPath = event.descriptor.taskPath
        logger.lifecycle("$taskPath > $summary")
    }
}
