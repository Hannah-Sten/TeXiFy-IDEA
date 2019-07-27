package nl.hannahsten.texifyidea.structure.latex

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * @author Hannah Schellekens
 */
object LatexPresentationFactory {

    @JvmStatic
    fun getPresentation(commands: LatexCommands) = when (commands.commandToken.text) {
        "\\part" -> LatexPartPresentation(commands)
        "\\chapter" -> LatexChapterPresentation(commands)
        "\\section" -> LatexSectionPresentation(commands)
        "\\subsection" -> LatexSubSectionPresentation(commands)
        "\\subsubsection" -> LatexSubSubSectionPresentation(commands)
        "\\paragraph" -> LatexParagraphPresentation(commands)
        "\\subparagraph" -> LatexSubParagraphPresentation(commands)
        "\\newcommand", "\\DeclareMathOperator" -> LatexNewCommandPresentation(commands)
        "\\label" -> LatexLabelPresentation(commands)
        "\\bibitem" -> BibitemPresentation(commands)
        "\\include", "\\includeonly", "\\input", "\\documentclass" -> LatexIncludePresentation(commands)
        else -> LatexOtherCommandPresentation(commands, TexifyIcons.DOT_COMMAND)
    }
}