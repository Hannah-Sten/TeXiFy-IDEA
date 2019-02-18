package nl.rubensten.texifyidea.intentions.latexmathtoggle

import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import nl.rubensten.texifyidea.lang.DefaultEnvironment
import nl.rubensten.texifyidea.lang.Package
import javax.swing.*

class MathEnvironmentDialog(private val envName: String?,
                            var result: String? = null) {

    init {
        DialogBuilder().apply {
            setTitle("Convert math environment")

            // Create components.
            val label = JLabel(
                    """|<html>
                        |<table>
                        |<tr><td>Please select a math environment from the list.</tr>
                        |</table>
                        |</html>""".trimMargin(),
                    SwingConstants.LEADING
            )

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
            panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
            panel.add(label)
            panel.add(comboBox)
            setCenterPanel(panel)

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