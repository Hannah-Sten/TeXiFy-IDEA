package nl.hannahsten.texifyidea.action.evince

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.action.EditorAction
import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer
import nl.hannahsten.texifyidea.settings.TexifySettings

/**
 * Starts a forward search action in Evince.
 *
 * Note: this is only available on Linux.
 *
 * @author Thomas Schouten
 */
open class ForwardSearchAction : EditorAction(
        "_ForwardSearch",
        TexifyIcons.RIGHT
) {

    val evince = PdfViewer.EVINCE

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        if (!evince.isAvailable()) {
            return
        }

        val document = textEditor.editor.document
        val line = document.getLineNumber(textEditor.editor.caretModel.offset) + 1

        evince.conversation!!.forwardSearch(pdfPath = null, sourceFilePath = file.path, line = line, project = project)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = TexifySettings.getInstance().pdfViewer == evince
    }
}