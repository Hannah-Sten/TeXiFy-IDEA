package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.run.latex.LatexStepType

internal object LatexStepPresentation {

    private val descriptions: Map<String, String> = mapOf(
        LatexStepType.LATEX_COMPILE to TexifyBundle.message("run.step.type.compile.latex"),
        LatexStepType.LATEXMK_COMPILE to TexifyBundle.message("run.step.type.compile.latexmk"),
        LatexStepType.EXTERNAL_TOOL to TexifyBundle.message("run.step.type.external.tool"),
        LatexStepType.MAKEINDEX to TexifyBundle.message("run.step.type.makeindex"),
        LatexStepType.BIBTEX to TexifyBundle.message("run.step.type.bibtex"),
        LatexStepType.PYTHONTEX to TexifyBundle.message("run.step.type.pythontex"),
        LatexStepType.MAKEGLOSSARIES to TexifyBundle.message("run.step.type.makeglossaries"),
        LatexStepType.XINDY to TexifyBundle.message("run.step.type.xindy"),
        LatexStepType.PDF_VIEWER to TexifyBundle.message("run.step.type.pdf.viewer"),
        LatexStepType.FILE_CLEANUP to TexifyBundle.message("run.step.type.file.cleanup"),
    )

    fun displayName(type: String): String = descriptions[type] ?: TexifyBundle.message("run.step.type.unsupported", type)
}
