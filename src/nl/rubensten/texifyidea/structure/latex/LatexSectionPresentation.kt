package nl.rubensten.texifyidea.structure.latex

import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.structure.EditableHintPresentation

/**
 * @author Ruben Schellekens
 */
class LatexSectionPresentation(sectionCommand: LatexCommands) : EditableHintPresentation {

    private val sectionName: String
    private var hint = ""

    init {
        if (sectionCommand.commandToken.text != "\\section") {
            throw IllegalArgumentException("command is no \\section-command")
        }

        this.sectionName = sectionCommand.requiredParameters[0]
    }

    override fun getPresentableText() = sectionName

    override fun getLocationString() = hint

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_SECTION!!

    override fun setHint(hint: String) {
        this.hint = hint
    }
}