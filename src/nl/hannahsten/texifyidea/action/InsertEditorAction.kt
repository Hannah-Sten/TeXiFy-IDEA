package nl.hannahsten.texifyidea.action

import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.isLatexOrBibtex
import javax.swing.Icon

/**
 * @author Hannah Schellekens
 */
open class InsertEditorAction(
    /**
     * The name of the action.
     */
    name: String,
    /**
     * The icon of the action.
     */
    icon: Icon?,
    /**
     * The text to insert before the selection.
     */
    before: String?,
    /**
     * The text to insert after the selection.
     */
    after: String?
) : EditorAction(name, icon) {

    /**
     * What to insert before the selection.
     */
    private val before: String = before ?: ""

    /**
     * What to insert after the selection.
     */
    private val after: String = after ?: ""

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {

        val editor = textEditor.editor
        val document = editor.document
        val selection = editor.selectionModel
        val start = selection.selectionStart
        val end = selection.selectionEnd

        // Don't touch any file content that is not related to TeXiFy
        if (file.psiFile(project)?.findElementAt(start)?.isLatexOrBibtex() != true) return

        runWriteAction(project, file) { insert(document, start, end, editor.caretModel) }
    }

    private fun insert(document: Document, start: Int, end: Int, caretModel: CaretModel) {
        document.insertString(end, this.after)
        document.insertString(start, this.before)

        val caretPosition = if (start == end) {
            start + this.before.length
        }
        else {
            end + this.before.length + this.after.length
        }

        caretModel.moveToOffset(caretPosition)
    }
}
