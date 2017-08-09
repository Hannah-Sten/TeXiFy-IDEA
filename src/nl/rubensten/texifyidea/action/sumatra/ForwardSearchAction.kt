package nl.rubensten.texifyidea.action.sumatra

import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import nl.rubensten.texifyidea.TeXception
import nl.rubensten.texifyidea.action.EditorAction
import nl.rubensten.texifyidea.run.SumatraConversation

/**
 * @author Sten Wessel
 */
class ForwardSearchAction : EditorAction("Forward search", null) {

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
}
