package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.ui.RunConfigurationEditorFragment
import com.intellij.util.ui.JBUI
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

internal class LatexCompileSequenceFragment(
    private val component: LatexCompileSequenceComponent,
) : RunConfigurationEditorFragment<LatexRunConfiguration, JComponent>(
    "compileSequence",
    "Compile sequence",
    null,
    wrap(component),
    0,
    { true }
) {

    init {
        component.changeListener = { fireEditorStateChanged() }
        isRemovable = false
        actionHint = "Add, remove, or reorder compile steps"
    }

    override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
        component.resetEditorFrom(s.configuration as LatexRunConfiguration)
    }

    override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
        component.applyEditorTo(s.configuration as LatexRunConfiguration)
    }

    companion object {

        private fun wrap(component: LatexCompileSequenceComponent): JComponent {
            val tooltip = "Drag to reorder. Double-click a step to change its type."
            val panel = JPanel(BorderLayout())
            panel.toolTipText = tooltip

            val label = JLabel("Compile sequence").apply {
                font = JBUI.Fonts.label().deriveFont(Font.BOLD)
                toolTipText = tooltip
            }
            component.toolTipText = tooltip

            panel.add(label, BorderLayout.NORTH)
            panel.add(component, BorderLayout.CENTER)

            return panel
        }
    }
}
