package nl.hannahsten.texifyidea.run.ui

import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.ui.RunConfigurationEditorFragment
import com.intellij.util.ui.JBUI
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class LatexCompileSequenceFragment(private val component: LatexCompileSequenceComponent)
    : RunConfigurationEditorFragment<LatexRunConfiguration, JComponent>(
        "compileSequence", null, null, wrap(component), 0, { true }
    ) {

    companion object {

        private fun wrap(component: LatexCompileSequenceComponent): JComponent {
            val panel = JPanel(BorderLayout())
            val label = JLabel("Compile sequence").apply {
                font = JBUI.Fonts.label().deriveFont(Font.BOLD)
            }
            panel.add(label, BorderLayout.NORTH)
            panel.add(component, BorderLayout.CENTER)

            return panel
        }
    }

    init {
        component.changeListener = { fireEditorStateChanged() }
        actionHint = "Specify steps needed for compiling the document"
    }

    override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
        component.apply(s.configuration as LatexRunConfiguration)
    }

    override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
        component.reset(s.configuration as LatexRunConfiguration)
    }
}
