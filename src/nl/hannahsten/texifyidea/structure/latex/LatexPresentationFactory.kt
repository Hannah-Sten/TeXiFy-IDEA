package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.lang.predefined.CommandNames
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.labels.getLabelPosition
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * @author Hannah Schellekens
 */
object LatexPresentationFactory {

    @JvmStatic
    fun getPresentation(commands: LatexCommands): ItemPresentation {
        // Any delay here will be a delay before the contents of the structure popup are shown

        val labelPosition = commands.getLabelPosition()
        if (labelPosition >= 0) {
            return LatexLabelPresentation(commands, labelPosition)
        }
        return CommandNames.run {
            when (commands.name) {
                PART -> LatexPartPresentation(commands)
                CHAPTER -> LatexChapterPresentation(commands)
                SECTION -> LatexSectionPresentation(commands)
                SUB_SECTION -> LatexSubSectionPresentation(commands)
                SUB_SUB_SECTION -> LatexSubSubSectionPresentation(commands)
                PARAGRAPH -> LatexParagraphPresentation(commands)
                SUB_PARAGRAPH -> LatexSubParagraphPresentation(commands)
                NEW_COMMAND, RENEW_COMMAND, DECLARE_MATH_OPERATOR, NEW_DOCUMENT_COMMAND -> LatexNewCommandPresentation(commands)
                DECLARE_PAIRED_DELIMITER, DECLARE_PAIRED_DELIMITER_X, DECLARE_PAIRED_DELIMITER_XPP -> LatexPairedDelimiterPresentation(commands)
                LABEL -> LatexLabelPresentation(commands, labelPosition)
                BIB_ITEM -> BibitemPresentation(commands)
                in CommandMagic.allFileIncludeCommands -> LatexIncludePresentation(commands)
                else -> LatexOtherCommandPresentation(commands, TexifyIcons.DOT_COMMAND)
            }
        }
    }
}