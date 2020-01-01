package nl.hannahsten.texifyidea.action.skim

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.action.EditorAction
import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer
import nl.hannahsten.texifyidea.settings.TexifySettings

/**
 * Starts a forward search action in Skim.
 *
 * Note: this is only available on MacOS.
 *
 * @author Stephan Sundermann
 */
open class ForwardSearchAction : EditorAction(
        "_ForwardSearch",
        TexifyIcons.RIGHT
) {
    private val skim = PdfViewer.SKIM

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        if (!skim.isAvailable()) {
            return
        }

        val document = textEditor.editor.document
        val line = document.getLineNumber(textEditor.editor.caretModel.offset) + 1

        skim.conversation!!.forwardSearch(null, file.path, line, project, focusAllowed = true)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = TexifySettings.getInstance().pdfViewer == skim
    }
}
