package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.icons.AllIcons
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepProviders
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepTypeInference
import javax.swing.Icon

internal object LatexStepUiSupport {

    private val knownDescriptions: Map<String, String> = mapOf(
        LatexStepType.LATEX_COMPILE to "Compile LaTeX",
        LatexStepType.LATEXMK_COMPILE to "Compile with latexmk",
        LatexStepType.EXTERNAL_TOOL to "Run external tool",
        LatexStepType.MAKEINDEX to "Run makeindex",
        LatexStepType.BIBTEX to "Run bibliography",
        LatexStepType.PYTHONTEX to "Run pythontex",
        LatexStepType.MAKEGLOSSARIES to "Run makeglossaries",
        LatexStepType.XINDY to "Run xindy",
        LatexStepType.PDF_VIEWER to "Open PDF viewer",
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
