package nl.hannahsten.texifyidea.structure.latex

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.structure.EditableHintPresentation

/**
 * @author Hannah Schellekens
 */
class LatexChapterPresentation(chapterCommand: LatexCommands) : EditableHintPresentation {

    private val chapterName: String
    private var hint = ""

    init {
        if (chapterCommand.commandToken.text != "\\chapter") {
            throw IllegalArgumentException("command is no \\chapter-command")
        }

        this.chapterName = chapterCommand.requiredParameters[0]
    }

    override fun getPresentableText() = chapterName

    override fun getLocationString() = hint

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_CHAPTER!!

    override fun setHint(hint: String) {
        this.hint = hint
    }
}