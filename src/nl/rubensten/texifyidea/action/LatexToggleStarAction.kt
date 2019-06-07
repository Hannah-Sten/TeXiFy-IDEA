package nl.rubensten.texifyidea.action

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.lang.magic.magicComment
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexGroup
import nl.rubensten.texifyidea.psi.LatexPsiUtil
import nl.rubensten.texifyidea.psi.LatexTypes
import nl.rubensten.texifyidea.util.parentOfType

/**
 * @author Ruben Schellekens
 */
class LatexToggleStarAction : EditorAction("Toggle Star", TexifyIcons.TOGGLE_STAR) {

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        val element = getElement(file, project, textEditor)
        val editor = textEditor.editor
        val psiFile = getPsiFile(file, project)

        element?.parentOfType(LatexGroup::class)?.let {
            println(it.magicComment())
        }

        val commands = LatexPsiUtil.getParentOfType(element, LatexCommands::class.java) ?: return

        runWriteAction(project) { toggleStar(editor, psiFile, commands) }
    }

    /**
     * Removes the star from a latex commands or adds it when there was no star in the first place.
     *
     * @param editor
     *          The current editor.
     * @param psiFile
     *          The current file.
     * @param commands
     *          The latex command to toggle the star of.
     */
    private fun toggleStar(editor: Editor, psiFile: PsiFile?, commands: LatexCommands?) {
        if (removeStar(commands!!)) {
            return
        }

        addStar(editor, psiFile, commands)
    }

    /**
     * Removes the star from a LaTeX command.
     *
     * @param commands
     *          The command to remove the star from.
     * @return `true` when the star was removed, `false` when the star was not removed.
     */
    private fun removeStar(commands: LatexCommands): Boolean {
        val lastChild = commands.lastChild
        var elt: PsiElement? = commands.firstChild
        while (elt !== lastChild && elt != null) {
            if (elt !is LeafPsiElement) {
                elt = elt.nextSibling
                continue
            }

            if (elt.elementType === LatexTypes.STAR) {
                elt.delete()
                return true
            }
            elt = elt.nextSibling
        }

        return false
    }

    /**
     * Adds a star to a latex command.
     *
     * @param editor
     *          The current editor.
     * @param file
     *          The current file.
     * @param commands
     *          The latex command to add a star to.
     */
    private fun addStar(editor: Editor, file: PsiFile?, commands: LatexCommands) {
        val document = editor.document
        var position = editor.caretModel.offset
        while (position > 0) {
            val text = document.getText(TextRange(position, position + 1))
            val elt = file!!.findElementAt(position)
            val parent = LatexPsiUtil.getParentOfType(elt, LatexCommands::class.java)

            if (text.equals("\\", ignoreCase = true) && (elt == null || parent === commands)) {
                document.insertString(position + commands.commandToken.text.length, "*")
                return
            }

            position--
        }
    }
}