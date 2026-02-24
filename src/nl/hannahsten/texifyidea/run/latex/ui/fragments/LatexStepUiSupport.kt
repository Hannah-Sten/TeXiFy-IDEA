package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.icons.AllIcons
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepProviders
import javax.swing.Icon

internal object LatexStepUiSupport {

    private val knownDescriptions: Map<String, String> = mapOf(
        "latex-compile" to "Compile LaTeX",
        "latexmk-compile" to "Compile with latexmk",
        "legacy-external-tool" to "Run external tool",
        "legacy-makeindex" to "Run makeindex",
        "legacy-bibtex" to "Run bibliography",
        "pythontex-command" to "Run pythontex",
        "makeglossaries-command" to "Run makeglossaries",
        "xindy-command" to "Run xindy",
        "pdf-viewer" to "Open PDF viewer",
    )

    fun availableStepTypes(): List<String> = LatexRunStepProviders.all.map { it.type }

    fun description(type: String): String = knownDescriptions[type] ?: "Unsupported step: $type"

    fun icon(type: String): Icon? = when {
        LatexRunStepProviders.find(type) != null -> null
        else -> AllIcons.General.Warning
    }

    fun inferStepTypesFromLegacyConfiguration(runConfig: LatexRunConfiguration): List<String> {
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
