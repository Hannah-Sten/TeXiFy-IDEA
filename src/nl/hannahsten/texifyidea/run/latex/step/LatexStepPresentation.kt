package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.TexifyBundle
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
        val descriptionKey: String,
        val createOptions: () -> LatexStepRunConfigurationOptions,
    )

    private val definitions: Map<String, StepDefinition> = mapOf(
        LatexStepType.LATEX_COMPILE to StepDefinition("run.step.type.compile.latex", ::LatexCompileStepOptions),
        LatexStepType.LATEXMK_COMPILE to StepDefinition("run.step.type.compile.latexmk", ::LatexmkCompileStepOptions),
        LatexStepType.EXTERNAL_TOOL to StepDefinition("run.step.type.external.tool", ::ExternalToolStepOptions),
        LatexStepType.MAKEINDEX to StepDefinition("run.step.type.makeindex", ::MakeindexStepOptions),
        LatexStepType.BIBTEX to StepDefinition("run.step.type.bibtex", ::BibtexStepOptions),
        LatexStepType.PYTHONTEX to StepDefinition("run.step.type.pythontex", ::PythontexStepOptions),
        LatexStepType.MAKEGLOSSARIES to StepDefinition("run.step.type.makeglossaries", ::MakeglossariesStepOptions),
        LatexStepType.XINDY to StepDefinition("run.step.type.xindy", ::XindyStepOptions),
        LatexStepType.PDF_VIEWER to StepDefinition("run.step.type.pdf.viewer", ::PdfViewerStepOptions),
        LatexStepType.FILE_CLEANUP to StepDefinition("run.step.type.file.cleanup", ::FileCleanupStepOptions),
    )

    fun displayName(type: String): String = definition(type)
        ?.descriptionKey
        ?.let(TexifyBundle::message)
        ?: TexifyBundle.message("run.step.type.unsupported", type)

    fun defaultStepFor(type: String): LatexStepRunConfigurationOptions? = definition(type)?.createOptions?.invoke()

    private fun definition(type: String): StepDefinition? = definitions[type.trim().lowercase()]
}
