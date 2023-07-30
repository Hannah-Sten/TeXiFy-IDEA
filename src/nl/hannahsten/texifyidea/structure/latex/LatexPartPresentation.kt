package nl.hannahsten.texifyidea.structure.latex

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.structure.EditableHintPresentation

/**
 * @author Hannah Schellekens
 */
class LatexPartPresentation(partCommand: LatexCommands) : EditableHintPresentation {

    private val partName: String
    private var hint = ""

    init {
        if (partCommand.commandToken.text != "\\part") {
            throw IllegalArgumentException("command is no \\part-command")
        }

        this.partName = partCommand.getRequiredParameters()[0]
    }

    override fun getPresentableText() = partName

    override fun getLocationString() = hint

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_PART

    override fun setHint(hint: String) {
        this.hint = hint
    }
}