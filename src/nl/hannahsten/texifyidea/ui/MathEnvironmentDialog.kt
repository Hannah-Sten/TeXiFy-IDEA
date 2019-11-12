package nl.hannahsten.texifyidea.ui

import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.lang.Environment
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class MathEnvironmentDialog(private val environmentName: String?, var result: String? = null) {

    init {
        DialogBuilder().apply {
            setTitle("Convert math environment")

            // Create components.
            val label = JLabel("Please select a math environment from the list.")
            val hint = JLabel("Use the arrow keys for easy selection.")
            hint.foreground = Color.GRAY

            // Get all the math environments.
            val environments: Array<String> = arrayOf(DefaultEnvironment.values()
                    .filter { it.context == Environment.Context.MATH }
                    .map { it.environmentName }
                    .toTypedArray(),
                    // Add the inline and display environments.
                    arrayOf("inline", "display"))
                    .flatten()
                    // Remove eqation*/displaymath, split/cases, and current environments.
                    .filter { it != "split" && it != "cases" && it != "equation*" && it != "displaymath" && it != environmentName}
                    .toTypedArray()
                    .sortedArray()

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
