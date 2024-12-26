package nl.hannahsten.texifyidea.structure.latex

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.structure.EditableHintPresentation

/**
 * @author Hannah Schellekens
 */
class LatexSubSectionPresentation(sectionCommand: LatexCommands) : EditableHintPresentation {

    private val subSectionName: String
    private var hint = ""

    init {
        this.subSectionName = sectionCommand.getRequiredParameters().firstOrNull() ?: "Unnamed subsection"
    }

    override fun getPresentableText() = subSectionName

    override fun getLocationString() = hint

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_SUBSECTION

    override fun setHint(hint: String) {
        this.hint = hint
    }
}