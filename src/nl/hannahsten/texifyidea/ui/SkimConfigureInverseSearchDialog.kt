package nl.hannahsten.texifyidea.ui

import com.intellij.openapi.ui.DialogBuilder
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * A dialog that contains a brief explanation about configuring inverse search with Skim.
 *
 * @author Stephan Sundermann
 */
class SkimConfigureInverseSearchDialog {

    init {
        DialogBuilder().apply {
            setTitle("Configure Inverse Search")

            val body = JLabel("<html>In the Skim settings, go to the Sync tab. Select Custom as Preset, and as a command provide 'idea' and set arguments to '--line %line %file' <br><br>See the wiki for more information.</html>")
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