package nl.hannahsten.texifyidea.ui

import com.intellij.openapi.ui.DialogBuilder
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * A dialog that contains a brief explanation about configuring inverse search with Okular.
 *
 * @author Abby Berkers
 */
class OkularConfigureInverseSearchDialog {
    init {
        DialogBuilder().apply {
            setTitle("Configure inverse search")

            val body = JLabel("<html>In the Okular settings, go to the Editor tab. Select the Custom Text Editor as Editor, and as a command provide 'idea --line %l' <br><br>See the wiki for more information.</html>")
            // Create panel.
            val panel = JPanel()
            panel.layout = BorderLayout()
            panel.add(body, BorderLayout.CENTER)
            setCenterPanel(panel)

            removeAllActions()
            addOkAction()
            show()
        }
    }
}