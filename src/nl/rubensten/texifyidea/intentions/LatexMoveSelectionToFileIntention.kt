package nl.rubensten.texifyidea.intentions

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.ui.CreateFileDialog
import nl.rubensten.texifyidea.util.createFile
import nl.rubensten.texifyidea.util.findRootFile
import nl.rubensten.texifyidea.util.isLatexFile
import nl.rubensten.texifyidea.util.removeIndents
import org.intellij.lang.annotations.Language
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.*

/**
 * @author Ruben Schellekens
 */
open class LatexMoveSelectionToFileIntention : TexifyIntentionBase("Move selection contents to seperate file") {

    companion object {

        private const val minimumSelectionLength = 24
    }

    override fun startInWriteAction() = false

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isLatexFile()) {
            return false
        }

        val selectionSize = selectionOffsets(editor).sumBy { (start, end) -> end - start }
        return selectionSize >= minimumSelectionLength
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) {
            return
        }

        // Ask the user for a new file name.
        val offsets = selectionOffsets(editor)
        val document = editor.document
        val filePath = CreateFileDialog(file.containingDirectory.virtualFile.canonicalPath, "")
                .newFileFullPath ?: return

        // Find text.
        val text = StringBuilder()
        for ((start, end) in offsets) {
            val selected = document.getText(TextRange(start, end)).trimEnd().removeIndents()
            text.append(selected)
        }

        // Manage paths/file names.
        @Language("RegExp")
        // Note that we do not override the user-specified filename to be LaTeX-like.
        val root = file.findRootFile().containingDirectory.virtualFile.canonicalPath

        // Execute write actions.
        runWriteAction {
            val createdFile = createFile("$filePath.tex", text.toString())

            for ((start, end) in offsets.reversed()) {
                document.deleteString(start, end)
            }

            val fileNameRelativeToRoot = createdFile.absolutePath.replace("$root/", "")
            document.insertString(offsets.first().first, "\\input{${fileNameRelativeToRoot.dropLast(4)}}")
        }
    }

    private fun selectionOffsets(editor: Editor): List<Pair<Int, Int>> {
        return editor.caretModel.allCarets
                .map { Pair(it.selectionStart, it.selectionEnd) }
    }

}