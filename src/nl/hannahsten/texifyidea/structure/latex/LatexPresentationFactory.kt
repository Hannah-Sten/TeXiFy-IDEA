package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.fileEditor.FileDocumentManager
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.introduces
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.lang.predefined.CommandNames
import nl.hannahsten.texifyidea.psi.contentText
import nl.hannahsten.texifyidea.psi.nameWithSlash
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil

/**
 * @author Hannah Schellekens
 */
object LatexPresentationFactory {

    @JvmStatic
    fun getPresentation(commands: LatexCommands): ItemPresentation {
        // Any delay here will be a delay before the contents of the structure popup are shown
        val nameWithSlash = commands.nameWithSlash
        val semantics = LatexDefinitionService.resolveCommand(commands)
        if (nameWithSlash in CommandMagic.labels || semantics?.introduces(LatexContexts.LabelDefinition) == true) {
            return buildLabelPresentation(commands, semantics)
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
                BIB_ITEM -> BibitemPresentation(commands)
                in CommandMagic.allFileIncludeCommands -> LatexIncludePresentation(commands)
                else -> LatexOtherCommandPresentation(commands, TexifyIcons.DOT_COMMAND)
            }
        }
    }

    private fun buildLabelPresentation(cmd: LatexCommands, semantics: LSemanticCommand?): ItemPresentation {
        fun getPresentableText(): String {
            if (semantics == null) {
                return cmd.requiredParameterText(0) ?: "no label found"
            }
            LatexPsiUtil.processArgumentsWithSemantics(cmd, semantics) { param, arg ->
                if (arg != null && arg.contextSignature.introduces(LatexContexts.LabelDefinition)) {
                    return param.contentText()
                }
            }
            return "no label found"
        }

        val presentableText: String = getPresentableText()

        // Location string.
        val manager = FileDocumentManager.getInstance()
        val document = manager.getDocument(cmd.containingFile.virtualFile)
        val line = document!!.getLineNumber(cmd.textOffset) + 1
        val locationString = cmd.containingFile.name + ":" + line

        return LatexLabelPresentation(locationString, presentableText)
    }
}