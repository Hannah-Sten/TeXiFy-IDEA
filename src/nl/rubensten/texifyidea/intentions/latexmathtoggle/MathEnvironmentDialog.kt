package nl.rubensten.texifyidea.intentions.latexmathtoggle

import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import nl.rubensten.texifyidea.lang.DefaultEnvironment
import nl.rubensten.texifyidea.lang.Package
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class MathEnvironmentDialog(private val envName: String?,
                            var result: String? = null) {

    init {
        DialogBuilder().apply {
            setTitle("Convert math environment")

            // Create components.
            val label = JLabel("Please select a math environment from the list.")
            val hint = JLabel("Use the arrow keys for easy selection.")
            hint.foreground = Color.GRAY

            // Get all the math environments.
            val environments: Array<String> = arrayOf(DefaultEnvironment.values()
                    .filter { it.dependency == Package.AMSMATH }
                    .map { it.environmentName }
                    .toTypedArray(),
                    // Add the inline and display environments.
                    arrayOf("inline", "display"))
                    .flatten()
                    // Remove split/cases, and current environments.
                    .filter { it != "split" && it != "cases" && it != envName}
                    .toTypedArray()

            val comboBox = JComboBox(environments)

            // Create panel.
            val panel = JPanel()
            panel.layout = BorderLayout()
            panel.add(label, BorderLayout.NORTH)
            panel.add(comboBox, BorderLayout.CENTER)
            setCenterPanel(panel)

            setPreferredFocusComponent(comboBox)

            // Dialog stuff.
            addOkAction()
            setOkOperation {
                dialogWrapper.close(0)
            }

            if (show() == DialogWrapper.OK_EXIT_CODE) {
                result = comboBox.selectedItem.toString()
            }
        }
    }
}
