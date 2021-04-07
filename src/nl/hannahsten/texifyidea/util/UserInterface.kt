package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.panels.HorizontalLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
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

/**
 * Adds a component to the panel with a label before it.
 *
 * @param component
 *          The component to add to the panel.
 * @param description
 *          The label to put before the component.
 * @param labelWidth
 *          The fixed label width, or `null` to use the label's inherent size.
 */
fun JPanel.addLabeledComponent(
        component: JComponent,
        description: String,
        labelWidth: Int? = null,
        leftPadding: Int = 16
): JPanel {
    // Uses a border layout with West for the label and Center for the control itself.
    // East is reserved for suffix elements.
    val pane = JPanel(BorderLayout()).apply {
        val label = JBLabel(description).apply {
            // Left padding.
            border = EmptyBorder(0, leftPadding, 0, 0)

            // Custom width if specified.
            labelWidth?.let {
                preferredSize = Dimension(it, height)
            }

            // Align top.
            alignmentY = 0.0f
        }
        add(label, BorderLayout.WEST)
        add(component, BorderLayout.CENTER)
    }
    add(pane)
    return pane
}

/**
 * Creates a jpanel with horizontally aligned elements.
 */
fun hbox(spacing: Int = 8, vararg components: JComponent) = JPanel(HorizontalLayout(spacing)).apply {
    components.forEach { add(it) }
}