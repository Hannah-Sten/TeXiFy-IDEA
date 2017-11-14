package nl.rubensten.texifyidea.structure.latex

import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.structure.EditableHintPresentation

/**
 * @author Ruben Schellekens
 */
class LatexPartPresentation(partCommand: LatexCommands) : EditableHintPresentation {

    private val partName: String
    private var hint = ""

    init {
        if (partCommand.commandToken.text != "\\part") {
            throw IllegalArgumentException("command is no \\part-command")
        }

        this.partName = partCommand.requiredParameters[0]
    }

    override fun getPresentableText() = partName

    override fun getLocationString() = hint

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_PART!!

    override fun setHint(hint: String) {
        this.hint = hint
    }
}