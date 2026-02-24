package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration

internal object LatexRunStepTypeInference {

    fun inferFromRunConfiguration(runConfig: LatexRunConfiguration): List<String> {
        val inferred = mutableListOf<String>()
        val compileType = when (runConfig.compiler) {
            LatexCompiler.LATEXMK -> "latexmk-compile"
            null -> null
            else -> "latex-compile"
        }

        if (compileType != null) {
            inferred += compileType
        }
        if (runConfig.externalToolRunConfigs.isNotEmpty()) {
            inferred += "legacy-external-tool"
        }
        if (runConfig.makeindexRunConfigs.isNotEmpty()) {
            inferred += "legacy-makeindex"
        }
        if (runConfig.bibRunConfigs.isNotEmpty()) {
            inferred += "legacy-bibtex"
        }
        if (runConfig.compileTwice && compileType == "latex-compile") {
            inferred += "latex-compile"
        }
        if (runConfig.pdfViewer != null || !runConfig.viewerCommand.isNullOrBlank()) {
            inferred += "pdf-viewer"
        }

        return inferred
    }
}
