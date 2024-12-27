package nl.hannahsten.texifyidea.structure.latex

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.structure.EditableHintPresentation

/**
 * @author Hannah Schellekens
 */
class LatexSectionPresentation(sectionCommand: LatexCommands) : EditableHintPresentation {

    private val sectionName = sectionCommand.getRequiredParameters().firstOrNull() ?: "Unnamed section"
    private var hint = ""

    override fun getPresentableText() = sectionName

    override fun getLocationString() = hint

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_SECTION

    override fun setHint(hint: String) {
        this.hint = hint
    }
}