package nl.hannahsten.texifyidea.structure.latex

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.structure.EditableHintPresentation
import nl.hannahsten.texifyidea.util.magic.cmd

/**
 * @author Hannah Schellekens
 */
class LatexChapterPresentation(chapterCommand: LatexCommands) : EditableHintPresentation {

    private val chapterName: String
    private var hint = ""

    init {
        if (chapterCommand.name != LatexGenericRegularCommand.CHAPTER.cmd) {
            throw IllegalArgumentException("command is no \\chapter-command")
        }

        this.chapterName = chapterCommand.getRequiredParameters().getOrElse(0) { "No chapter name" }
    }

    override fun getPresentableText() = chapterName

    override fun getLocationString() = hint

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_CHAPTER

    override fun setHint(hint: String) {
        this.hint = hint
    }
}