package nl.rubensten.texifyidea.structure.latex

import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.structure.EditableHintPresentation

/**
 * @author Ruben Schellekens
 */
class LatexSubSubSectionPresentation(sectionCommand: LatexCommands) : EditableHintPresentation {

    private val subSubSectionName: String
    private var hint = ""

    init {
        if (sectionCommand.commandToken.text != "\\subsubsection") {
            throw IllegalArgumentException("command is no \\subsubsection-command")
        }

        this.subSubSectionName = sectionCommand.requiredParameters[0]
    }

    override fun getPresentableText() = subSubSectionName

    override fun getLocationString() = hint

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_SUBSUBSECTION!!

    override fun setHint(hint: String) {
        this.hint = hint
    }
}
