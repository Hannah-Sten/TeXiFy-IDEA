package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.icons.AllIcons
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepProviders
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepTypeInference
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

    fun inferStepTypesFromLegacyConfiguration(runConfig: LatexRunConfiguration): List<String> =
        LatexRunStepTypeInference.inferFromRunConfiguration(runConfig)
}
