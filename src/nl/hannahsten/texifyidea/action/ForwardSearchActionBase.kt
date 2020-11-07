package nl.hannahsten.texifyidea.action

import com.intellij.execution.RunManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.editor.autocompile.AutoCompileState
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer
import nl.hannahsten.texifyidea.settings.TexifySettings

open class ForwardSearchActionBase(val viewer: PdfViewer) : EditorAction(
    name = "_ForwardSearch",
    icon = TexifyIcons.RIGHT
) {
    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        if (!viewer.isAvailable()) {
            return
        }

        val document = textEditor.editor.document
        val line = document.getLineNumber(textEditor.editor.caretModel.offset) + 1

        viewer.conversation!!.forwardSearch(null, file.path, line, project, focusAllowed = true)
    }

    override fun update(e: AnActionEvent) {
        val runConfig = e.project?.let {
            RunManager.getInstance(it).selectedConfiguration
        }
        e.presentation.isEnabledAndVisible = (runConfig?.configuration as? LatexRunConfiguration)?.pdfViewer == viewer
    }
}