package nl.hannahsten.texifyidea.structure.latex

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.structure.EditableHintPresentation

/**
 * @author Hannah Schellekens
 */
class LatexSubParagraphPresentation(subParagraphCommand: LatexCommands) : EditableHintPresentation {

    private val subParagraphName = subParagraphCommand.requiredParameterText(0) ?: "Unknown subparagraph"
    private var hint = ""

    override fun getPresentableText() = subParagraphName

    override fun getLocationString() = hint

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_SUBPARAGRAPH

    override fun setHint(hint: String) {
        this.hint = hint
    }
}