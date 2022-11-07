package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.ui.CreateFileDialog
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.files.writeToFileUndoable
import nl.hannahsten.texifyidea.util.removeIndents
import org.intellij.lang.annotations.Language
import java.io.File

/**
 * @author Hannah Schellekens
 */
open class LatexMoveSelectionToFileIntention : TexifyIntentionBase("Move selection contents to separate file") {

    companion object {

        private const val minimumSelectionLength = 24
    }

    override fun startInWriteAction() = false

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isLatexFile()) {
            return false
        }

        val selectionSize = selectionOffsets(editor).sumOf { (start, end): Pair<Int, Int> -> end - start }
        return selectionSize >= minimumSelectionLength
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) {
            return
        }

        // Ask the user for a new file name.
        val offsets = selectionOffsets(editor)
        val document = editor.document

        // Display a dialog to ask for the location and name of the new file.
        val filePath = CreateFileDialog(file.containingDirectory?.virtualFile?.canonicalPath, "")
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
        // Path of virtual file always contains '/' as file separators.
        val root = file.findRootFile().containingDirectory?.virtualFile?.canonicalPath ?: return

        // Execute write actions.
        runWriteAction {
            val createdFile = File(writeToFileUndoable(project, filePath, text.toString(), root))

            for ((start, end) in offsets.reversed()) {
                document.deleteString(start, end)
            }

            // The path of the created file contains the system's file separators, whereas the path of the root
            // (virtual file) always contains '/' as file separators.
            val fileNameRelativeToRoot = createdFile.absolutePath
                .replace(File.separator, "/")
                .replace("$root/", "")
            document.insertString(offsets.first().first, "\\input{${fileNameRelativeToRoot.dropLast(4)}}")
        }
    }

    private fun selectionOffsets(editor: Editor): List<Pair<Int, Int>> {
        return editor.caretModel.allCarets
            .map { Pair(it.selectionStart, it.selectionEnd) }
    }
}