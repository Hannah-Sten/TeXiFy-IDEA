package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.util.ui.JBUI
import java.awt.FlowLayout
import javax.swing.JLabel
import javax.swing.JPanel

internal class LatexUnsupportedStepSettingsComponent : JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)) {

    private val messageLabel = JLabel("Select a step in Compile sequence to configure it.")

    init {
        border = JBUI.Borders.empty(8, 0)
        add(messageLabel)
    }

    fun setMessage(message: String) {
        messageLabel.text = message
    }
}
