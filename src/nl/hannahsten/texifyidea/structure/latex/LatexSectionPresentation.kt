package nl.hannahsten.texifyidea.structure.latex

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.structure.EditableHintPresentation

/**
 * @author Hannah Schellekens
 */
class LatexSectionPresentation(sectionCommand: LatexCommands) : EditableHintPresentation {

    private val sectionName: String
    private var hint = ""

    init {
        if (sectionCommand.commandToken.text != "\\section") {
            throw IllegalArgumentException("command is no \\section-command")
        }

        this.sectionName = sectionCommand.getRequiredParameters().firstOrNull() ?: "Unnamed section"
    }

    override fun getPresentableText() = sectionName

    override fun getLocationString() = hint

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_SECTION

    override fun setHint(hint: String) {
        this.hint = hint
    }
}