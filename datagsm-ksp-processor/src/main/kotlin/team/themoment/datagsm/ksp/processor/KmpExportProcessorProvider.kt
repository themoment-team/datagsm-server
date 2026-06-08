package team.themoment.datagsm.ksp.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import java.io.File

class KmpExportProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val outputDir =
            environment.options["kmpOutputDir"]
                ?: error("KmpExport: kmpOutputDir option is required")
        val tsOutputDir = environment.options["tsOutputDir"]?.let { File(it) }
        return KmpExportProcessor(environment.logger, File(outputDir), tsOutputDir)
    }
}
