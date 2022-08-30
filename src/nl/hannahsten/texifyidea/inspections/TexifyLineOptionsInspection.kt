package nl.hannahsten.texifyidea.inspections

import com.intellij.ui.IdeBorderFactory
import org.jdom.Element
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea

/**
 * An inspection that carries an extra text area that serves as line input.
 *
 * @author Hannah Schellekens
 */
abstract class TexifyLineOptionsInspection(val title: String) : TexifyInspectionBase() {

    /**
     * TextArea that contains all the line options.
     */
    private val txtaOptions = JTextArea()

    /**
     * Key that is used to read and write from settings.
     */
    private val attributeKey = "texify.inspection.latex.$shortName.textArea"

    /**
     * All the nonempty lines in the text area.
     */
    protected val lines: List<String>
        get() = txtaOptions.text.trim().split("\n").asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()

    override fun createOptionsPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.add(txtaOptions)
        panel.border = IdeBorderFactory.createTitledBorder(title)
        return panel
    }

    override fun writeSettings(node: Element) {
        node.setAttribute(attributeKey, txtaOptions.text)
        super.writeSettings(node)
    }

    override fun readSettings(node: Element) {
        val value = node.getAttributeValue(attributeKey)
        if (value != null) {
            txtaOptions.text = value
        }

        super.readSettings(node)
    }
}
