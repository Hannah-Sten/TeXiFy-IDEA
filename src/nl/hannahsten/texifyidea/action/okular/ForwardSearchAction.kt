package nl.hannahsten.texifyidea.action.okular

import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.action.EditorAction

open class ForwardSearchAction : EditorAction(
        "_ForwardSearch",
        TexifyIcons.RIGHT
) {
    override fun actionPerformed(file: VirtualFile?, project: Project?, editor: TextEditor?) {
        // Todo forward search.
    }
}