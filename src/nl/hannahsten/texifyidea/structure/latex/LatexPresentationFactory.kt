package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand.*
import nl.hannahsten.texifyidea.lang.commands.LatexMathtoolsRegularCommand.*
import nl.hannahsten.texifyidea.lang.commands.LatexNewDefinitionCommand
import nl.hannahsten.texifyidea.lang.commands.LatexNewDefinitionCommand.NEWCOMMAND
import nl.hannahsten.texifyidea.lang.commands.LatexNewDefinitionCommand.RENEWCOMMAND
import nl.hannahsten.texifyidea.lang.commands.LatexXparseCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.getIncludeCommands
import nl.hannahsten.texifyidea.util.labels.getLabelDefinitionCommands
import nl.hannahsten.texifyidea.util.magic.cmd

/**
 * @author Hannah Schellekens
 */
object LatexPresentationFactory {

    @JvmStatic
    fun getPresentation(commands: LatexCommands): ItemPresentation {
        val labelingCommands = commands.project.getLabelDefinitionCommands()
        if (labelingCommands.contains(commands.name)) {
            return LatexLabelPresentation(commands)
        }
        return when (commands.name) {
            PART.cmd -> LatexPartPresentation(commands)
            CHAPTER.cmd -> LatexChapterPresentation(commands)
            SECTION.cmd -> LatexSectionPresentation(commands)
            SUBSECTION.cmd -> LatexSubSectionPresentation(commands)
            SUBSUBSECTION.cmd -> LatexSubSubSectionPresentation(commands)
            PARAGRAPH.cmd -> LatexParagraphPresentation(commands)
            SUBPARAGRAPH.cmd -> LatexSubParagraphPresentation(commands)
            NEWCOMMAND.cmd, RENEWCOMMAND.cmd, DECLARE_MATH_OPERATOR.cmd, LatexXparseCommand.NEWDOCUMENTCOMMAND.cmd -> LatexNewCommandPresentation(commands)
            DECLARE_PAIRED_DELIMITER.cmd, DECLARE_PAIRED_DELIMITER_X.cmd, DECLARE_PAIRED_DELIMITER_XPP.cmd -> LatexPairedDelimiterPresentation(
                commands
            )
            LABEL.cmd -> LatexLabelPresentation(commands)
            BIBITEM.cmd -> BibitemPresentation(commands)
            in getIncludeCommands() -> LatexIncludePresentation(commands)
            else -> LatexOtherCommandPresentation(commands, TexifyIcons.DOT_COMMAND)
        }
    }
}