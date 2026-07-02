package nl.hannahsten.texifyidea.ui

import com.intellij.openapi.ui.DialogBuilder
import nl.hannahsten.texifyidea.TexifyBundle
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
            setTitle(TexifyBundle.message("ui.dialog.configure.inverse.search.title"))

            val body = JLabel(TexifyBundle.message("ui.dialog.configure.inverse.search.skim.body.html"))
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
