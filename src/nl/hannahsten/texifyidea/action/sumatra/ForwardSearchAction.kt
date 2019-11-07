package nl.hannahsten.texifyidea.action.sumatra

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.action.EditorAction
import nl.hannahsten.texifyidea.run.sumatra.SumatraConversation
import nl.hannahsten.texifyidea.run.sumatra.isSumatraAvailable

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
        "_Forward Search",
        TexifyIcons.RIGHT
) {

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        if (!isSumatraAvailable) {
            return
        }

        val document = textEditor.editor.document
        val line = document.getLineNumber(textEditor.editor.caretModel.offset) + 1

        try {
            SumatraConversation.forwardSearch(sourceFilePath = file.path, line = line)
        }
        catch (ignored: TeXception) {
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = isSumatraAvailable
    }
}
