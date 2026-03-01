package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.run.latex.LatexStepType

internal object LatexStepPresentation {

    private val descriptionKeys: Map<String, String> = mapOf(
        LatexStepType.LATEX_COMPILE to "run.step.type.compile.latex",
        LatexStepType.LATEXMK_COMPILE to "run.step.type.compile.latexmk",
        LatexStepType.EXTERNAL_TOOL to "run.step.type.external.tool",
        LatexStepType.MAKEINDEX to "run.step.type.makeindex",
        LatexStepType.BIBTEX to "run.step.type.bibtex",
        LatexStepType.PYTHONTEX to "run.step.type.pythontex",
        LatexStepType.MAKEGLOSSARIES to "run.step.type.makeglossaries",
        LatexStepType.XINDY to "run.step.type.xindy",
        LatexStepType.PDF_VIEWER to "run.step.type.pdf.viewer",
        LatexStepType.FILE_CLEANUP to "run.step.type.file.cleanup",
    )

    fun displayName(type: String): String = descriptionKeys[type]?.let(TexifyBundle::message)
        ?: TexifyBundle.message("run.step.type.unsupported", type)
}
