package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.awt.RelativePoint
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.JTextComponent

/**
 * Shows a toast message above the loading bar.
 *
 * @param project
 *          The current project.
 * @param type
 *          The type of toast to toast.
 * @param htmlMessage
 *          What to show in the toast. Supports HTML.
 */
fun toast(project: Project, type: MessageType, htmlMessage: String) {
    val statusBar = WindowManager.getInstance().getStatusBar(project)
    JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder(htmlMessage, type, null)
            .setFadeoutTime(7500)
            .createBalloon()
            .show(RelativePoint.getCenterOf(statusBar.component), Balloon.Position.above)
}

/**
 * Shows an information toast message above the loading bar.
 *
 * @param project
 *          The current project.
 * @param htmlMessage
 *          What to show in the toast. Supports HTML.
 */
fun toastInfo(project: Project, htmlMessage: String) = toast(project, MessageType.INFO, htmlMessage)

/**
 * Shows an error toast message above the loading bar.
 *
 * @param project
 *          The current project.
 * @param htmlMessage
 *          What to show in the toast. Supports HTML.
 */
fun toastError(project: Project, htmlMessage: String) = toast(project, MessageType.ERROR, htmlMessage)

/**
 * Shows a warning information toast message above the loading bar.
 *
 * @param project
 *          The current project.
 * @param htmlMessage
 *          What to show in the toast. Supports HTML.
 */
fun toastWarning(project: Project, htmlMessage: String) = toast(project, MessageType.WARNING, htmlMessage)

/**
 * Adds a text listener to the component.
 *
 * @param event
 *          The function to execute when any text updates in the component.
 * @return The document listener that was added to the component's document.
 */
fun JTextComponent.addTextChangeListener(event: (DocumentEvent?) -> Unit): DocumentListener {
    val documentListener = object : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) {
            event(e)
        }

        override fun removeUpdate(e: DocumentEvent?) {
            event(e)
        }

        override fun changedUpdate(e: DocumentEvent?) {
            event(e)
        }
    }
    document.addDocumentListener(documentListener)
    return documentListener
}

/**
 * Adds a key typed listener to the component.
 *
 * @param event
 *          The function to execute when any key is typed.
 * @return The key listener that was added to the component.
 */
fun JTextComponent.addKeyTypedListener(event: (KeyEvent) -> Unit): KeyListener {
    val adapter = object : KeyAdapter() {
        override fun keyTyped(e: KeyEvent?) {
            e?.let { event(it) }
        }
    }
    addKeyListener(adapter)
    return adapter
}

/**
 * Adds a key released listener to the component.
 *
 * @param event
 *          The function to execute when any key is released.
 * @return The key listener that was added to the component.
 */
fun JTextComponent.addKeyReleasedListener(event: (KeyEvent) -> Unit): KeyListener {
    val adapter = object : KeyAdapter() {
        override fun keyReleased(e: KeyEvent?) {
            e?.let { event(it) }
        }
    }
    addKeyListener(adapter)
    return adapter
}

/**
 * Only allows the given characters to be typed into the component.
 *
 * @return The KeyListener that is used for the filter.
 */
fun JTextComponent.setInputFilter(allowedCharacters: Set<Char>) = addKeyTypedListener {
    if (it.keyChar !in allowedCharacters) {
        it.consume()
    }
}