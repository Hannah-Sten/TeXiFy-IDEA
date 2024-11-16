package nl.hannahsten.texifyidea.structure.latex

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.structure.EditableHintPresentation

/**
 * @author Hannah Schellekens
 */
class LatexSubParagraphPresentation(subParagraphCommand: LatexCommands) : EditableHintPresentation {

    private val subParagraphName: String
    private var hint = ""

    init {
        if (subParagraphCommand.name != "\\subparagraph") {
            throw IllegalArgumentException("command is no \\subparagraph-command")
        }

        this.subParagraphName = subParagraphCommand.getRequiredParameters().firstOrNull() ?: "Unknown subparagraph"
    }

    override fun getPresentableText() = subParagraphName

    override fun getLocationString() = hint

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_SUBPARAGRAPH

    override fun setHint(hint: String) {
        this.hint = hint
    }
}