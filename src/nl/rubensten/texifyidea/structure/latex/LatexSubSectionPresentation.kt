package nl.rubensten.texifyidea.structure.latex

import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.structure.EditableHintPresentation

/**
 * @author Ruben Schellekens
 */
class LatexSubSectionPresentation(sectionCommand: LatexCommands) : EditableHintPresentation {

    private val subSectionName: String
    private var hint = ""

    init {
        if (sectionCommand.commandToken.text != "\\subsection") {
            throw IllegalArgumentException("command is no \\subsection-command")
        }

        this.subSectionName = sectionCommand.requiredParameters[0]
    }

    override fun getPresentableText() = subSectionName

    override fun getLocationString() = hint

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_SUBSECTION!!

    override fun setHint(hint: String) {
        this.hint = hint
    }
}