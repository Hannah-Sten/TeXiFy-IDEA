package nl.rubensten.texifyidea.structure.latex

import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.structure.EditableHintPresentation

/**
 * @author Ruben Schellekens
 */
class LatexSubParagraphPresentation(subParagraphCommand: LatexCommands) : EditableHintPresentation {

    private val subParagraphName: String
    private var hint = ""

    init {
        if (subParagraphCommand.commandToken.text != "\\subparagraph") {
            throw IllegalArgumentException("command is no \\subparagraph-command")
        }

        this.subParagraphName = subParagraphCommand.requiredParameters[0]
    }

    override fun getPresentableText() = subParagraphName

    override fun getLocationString() = hint

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_SUBPARAGRAPH!!

    override fun setHint(hint: String) {
        this.hint = hint
    }
}