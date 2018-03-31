package nl.rubensten.texifyidea.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JComponent

/**
 * Tracks the shift key.
 *
 * @author Ruben Schellekens
 */
object ShiftTracker : KeyListener, TypedHandlerDelegate() {

    /**
     * `true` if the shift key is pressed, `false` if the shift key is not pressed.
     */
    private var shift = false

    /**
     * Set of all components that have been tracked.
     */
    private var registered: MutableSet<JComponent> = HashSet()

    /**
     * Setsup the shift tracker.
     */
    @JvmStatic
    fun setup(component: JComponent) {
        if (registered.contains(component)) {
            return
        }

        component.addKeyListener(this)
        registered.add(component)
    }

    /**
     * Checks if the shift key is pressed.
     *
     * @return `true` when shift is pressed, `false` when shift is not pressed.
     */
    @JvmStatic
    fun isShiftPressed(): Boolean = shift

    override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
        setup(editor.contentComponent)
        return super.beforeCharTyped(c, project, editor, file, fileType)
    }

    override fun keyTyped(e: KeyEvent?) {
        // Do nothing.
    }

    override fun keyPressed(e: KeyEvent?) {
        if (e?.keyCode == KeyEvent.VK_SHIFT) {
            shift = true
        }
    }

    override fun keyReleased(e: KeyEvent?) {
        if (e?.keyCode == KeyEvent.VK_SHIFT) {
            shift = false
        }
    }
}