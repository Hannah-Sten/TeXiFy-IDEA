package nl.hannahsten.texifyidea.action.okular

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.action.EditorAction
import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer
import nl.hannahsten.texifyidea.settings.TexifySettings

/**
 * Starts a forward search action in Okular.
 *
 * Note: this is only available on Linux.
 *
 * @author Abby Berkers
 */
open class ForwardSearchAction : EditorAction(
        "_ForwardSearch",
        TexifyIcons.RIGHT
) {
    val okular = PdfViewer.OKULAR

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        if (!okular.isAvailable()) {
            return
        }

        val document = textEditor.editor.document
        val line = document.getLineNumber(textEditor.editor.caretModel.offset) + 1

        okular.conversation!!.forwardSearch(null, file.path, line, project)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = TexifySettings.getInstance().pdfViewer == okular
    }
}