package nl.rubensten.texifyidea.action.sumatra

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import nl.rubensten.texifyidea.TeXception
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.action.EditorAction
import nl.rubensten.texifyidea.run.SumatraConversation

/**
 * Starts a forward search action in SumatraPDF.
 *
 * This action sends a command to SumatraPDF which attempts to match the line where the cursor is at in the source to
 * the line in the PDF. Note: this is only available on Windows.
 *
 * @author Sten Wessel
 * @since b0.4
 */
open class ForwardSearchAction : EditorAction(
        "ForwardSearch",
        TexifyIcons.RIGHT
) {

    override fun actionPerformed(file: VirtualFile, project: Project, editor: TextEditor) {
        if (!SystemInfo.isWindows) {
            return
        }

        val document = editor.editor.document
        val line = document.getLineNumber(editor.editor.caretModel.offset) + 1

        try {
            SumatraConversation.forwardSearch(sourceFilePath = file.path, line = line)
        } catch (ignored: TeXception) {

        }
    }

    override fun update(e: AnActionEvent?) {
        val presentation = e?.presentation ?: return
        presentation.isEnabledAndVisible = SystemInfo.isWindows
    }
}
