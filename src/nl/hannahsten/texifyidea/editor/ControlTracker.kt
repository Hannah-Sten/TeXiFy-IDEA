package nl.hannahsten.texifyidea.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JComponent

/**
 * Tracks the control key.
 *
 * @see ShiftTracker
 */
object ControlTracker : KeyListener, TypedHandlerDelegate() {

    /**
     * `true` if the control key is pressed, `false` if the control key is not pressed.
     */
    var isControlPressed = false
        private set

    /**
     * Set of all components that have been tracked.
     */
    private var registered: MutableSet<JComponent> = HashSet()

    /**
     * Set up the control tracker.
     */
    @JvmStatic
    fun setup(component: JComponent) {
        if (registered.contains(component)) {
            return
        }

        component.addKeyListener(this)
        registered.add(component)
    }

    override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
        setup(editor.contentComponent)
        return super.beforeCharTyped(c, project, editor, file, fileType)
    }

    override fun keyTyped(e: KeyEvent?) {
        // Do nothing.
    }

    override fun keyPressed(e: KeyEvent?) {
        if (e?.keyCode == KeyEvent.VK_CONTROL) {
            isControlPressed = true
        }
    }

    override fun keyReleased(e: KeyEvent?) {
        if (e?.keyCode == KeyEvent.VK_CONTROL) {
            isControlPressed = false
        }
    }
}
