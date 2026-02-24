package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.ui.RunConfigurationEditorFragment
import com.intellij.util.ui.JBUI
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

internal class LatexStepSettingsFragment(
    private val component: LatexStepSettingsComponent,
) : RunConfigurationEditorFragment<LatexRunConfiguration, JComponent>(
    "stepSettings",
    "Step settings",
    null,
    wrap(component),
    0,
    { true }
) {

    init {
        component.changeListener = { fireEditorStateChanged() }
        isRemovable = false
        setHint("Configure the selected compile step type")
    }

    override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
        component.resetEditorFrom(s.configuration as LatexRunConfiguration)
    }

    override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
        component.applyEditorTo(s.configuration as LatexRunConfiguration)
    }

    companion object {

        private fun wrap(component: LatexStepSettingsComponent): JComponent {
            val panel = JPanel(BorderLayout())

            val label = JLabel("Step settings").apply {
                font = JBUI.Fonts.label().deriveFont(Font.BOLD)
            }
            val hintLabel = JLabel("Select a step above to edit its type-level settings.").apply {
                font = JBUI.Fonts.label().deriveFont(Font.ITALIC)
                foreground = Color.LIGHT_GRAY
            }

            panel.add(label, BorderLayout.NORTH)
            panel.add(component, BorderLayout.CENTER)
            panel.add(hintLabel, BorderLayout.SOUTH)

            return panel
        }
    }
}
