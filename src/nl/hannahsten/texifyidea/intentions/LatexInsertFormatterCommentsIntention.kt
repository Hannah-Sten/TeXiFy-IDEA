package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic

class LatexInsertFormatterCommentsIntention : TexifyIntentionBase("Insert comments to disable the formatter.") {

    private val onTag = CodeStyleSettings.getDefaults().FORMATTER_ON_TAG
    private val offTag = CodeStyleSettings.getDefaults().FORMATTER_OFF_TAG

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        val beginName = file?.findElementAt(editor?.caretOffset() ?: return false)
            ?.parentOfType(LatexBeginCommand::class)
            ?.environmentName()
            ?: return false
        return EnvironmentMagic.isProbablyVerbatim(beginName)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        val beginCommand = file?.findElementAt(editor?.caretOffset() ?: return)
            ?.parentOfType(LatexBeginCommand::class)
        val endCommand = beginCommand?.endCommand() ?: return

        val indent: String = editor?.document?.lineIndentationByOffset(beginCommand.textOffset) ?: return
        val offComment = "% $offTag\n$indent"
        val onComment = "\n$indent% $onTag"

        editor.insertAndMove(beginCommand.textOffset, offComment)
        editor.insertAndMove(endCommand.endOffset() + offComment.length, onComment)
    }
}