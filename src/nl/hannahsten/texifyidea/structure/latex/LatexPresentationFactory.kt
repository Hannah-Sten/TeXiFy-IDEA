package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.getIncludeCommands

/**
 * @author Hannah Schellekens
 */
object LatexPresentationFactory {

    @JvmStatic
    fun getPresentation(commands: LatexCommands) : ItemPresentation {
        val labelingCommands = TexifySettings.getInstance().labelCommands
        if (labelingCommands.containsKey(commands.name)) {
            return LatexLabelPresentation(commands)
        }
        return when (commands.name) {
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
            in getIncludeCommands() -> LatexIncludePresentation(commands)
            else -> LatexOtherCommandPresentation(commands, TexifyIcons.DOT_COMMAND)
        }
    }
}