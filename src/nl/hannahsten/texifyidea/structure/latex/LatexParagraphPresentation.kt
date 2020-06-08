package nl.hannahsten.texifyidea.structure.latex

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.structure.EditableHintPresentation

/**
 * @author Hannah Schellekens
 */
class LatexParagraphPresentation(paragraphCommand: LatexCommands) : EditableHintPresentation {

    private val paragraphName: String
    private var hint = ""

    init {
        if (paragraphCommand.commandToken.text != "\\paragraph") {
            throw IllegalArgumentException("command is no \\paragraph-command")
        }

        if (paragraphCommand.requiredParameters.isEmpty()) {
            this.paragraphName = ""
        }
        else {
            this.paragraphName = paragraphCommand.requiredParameters[0]
        }
    }

    override fun getPresentableText() = paragraphName

    override fun getLocationString() = hint

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_PARAGRAPH

    override fun setHint(hint: String) {
        this.hint = hint
    }
}
