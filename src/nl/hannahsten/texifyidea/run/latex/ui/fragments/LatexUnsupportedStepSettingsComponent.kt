package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.util.ui.JBUI
import nl.hannahsten.texifyidea.TexifyBundle
import java.awt.FlowLayout
import javax.swing.JLabel
import javax.swing.JPanel

internal class LatexUnsupportedStepSettingsComponent : JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)) {

    private val messageLabel = JLabel(TexifyBundle.message("run.step.ui.step.settings.select.step"))

    init {
        border = JBUI.Borders.empty(8, 0)
        add(messageLabel)
    }

    fun setMessage(message: String) {
        messageLabel.text = message
    }

    fun message(): String = messageLabel.text
}
