package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.BibtexStepOptions
import nl.hannahsten.texifyidea.run.latex.ExternalToolStepOptions
import nl.hannahsten.texifyidea.run.latex.FileCleanupStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.MakeglossariesStepOptions
import nl.hannahsten.texifyidea.run.latex.MakeindexStepOptions
import nl.hannahsten.texifyidea.run.latex.PdfViewerStepOptions
import nl.hannahsten.texifyidea.run.latex.PythontexStepOptions
import nl.hannahsten.texifyidea.run.latex.XindyStepOptions

internal object LatexStepPresentation {

    private data class StepDefinition(
        val description: String,
        val createOptions: () -> LatexStepRunConfigurationOptions,
    )

    private val definitions: Map<String, StepDefinition> = mapOf(
        LatexStepType.LATEX_COMPILE to StepDefinition("Compile LaTeX", ::LatexCompileStepOptions),
        LatexStepType.LATEXMK_COMPILE to StepDefinition("Compile with latexmk", ::LatexmkCompileStepOptions),
        LatexStepType.EXTERNAL_TOOL to StepDefinition("Run external tool", ::ExternalToolStepOptions),
        LatexStepType.MAKEINDEX to StepDefinition("Run makeindex", ::MakeindexStepOptions),
        LatexStepType.BIBTEX to StepDefinition("Run bibliography", ::BibtexStepOptions),
        LatexStepType.PYTHONTEX to StepDefinition("Run pythontex", ::PythontexStepOptions),
        LatexStepType.MAKEGLOSSARIES to StepDefinition("Run makeglossaries", ::MakeglossariesStepOptions),
        LatexStepType.XINDY to StepDefinition("Run xindy", ::XindyStepOptions),
        LatexStepType.PDF_VIEWER to StepDefinition("Open PDF viewer", ::PdfViewerStepOptions),
        LatexStepType.FILE_CLEANUP to StepDefinition("Clean temporary build files", ::FileCleanupStepOptions),
    )

    fun displayName(type: String): String = definition(type)?.description ?: "Unsupported step: $type"

    fun defaultStepFor(type: String): LatexStepRunConfigurationOptions? = definition(type)?.createOptions?.invoke()

    private fun definition(type: String): StepDefinition? = definitions[type.trim().lowercase()]
}
