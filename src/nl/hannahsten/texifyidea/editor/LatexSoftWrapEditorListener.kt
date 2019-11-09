package nl.hannahsten.texifyidea.editor

import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.files.psiFile

/**
 * Enables automatic soft wrap when a LaTeX file is opened.
 *
 * @author Sten Wessel
 */
class LatexSoftWrapEditorListener : EditorFactoryListener {

    override fun editorCreated(event: EditorFactoryEvent) {
        if (!TexifySettings.getInstance().automaticSoftWraps) {
            return
        }

        val editor = event.editor

        if (editor.document.psiFile(editor.project ?: return)?.isLatexFile() != true) {
            return
        }

        editor.settings.apply {
            isUseSoftWraps = true
            isUseCustomSoftWrapIndent = true
        }
    }

    override fun editorReleased(event: EditorFactoryEvent) {
    }
}
