package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.ui.RunConfigurationEditorFragment
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import javax.swing.JComponent

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
    }

    override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
        component.resetEditorFrom(s.configuration as LatexRunConfiguration)
    }

    override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
        component.applyEditorTo()
    }

    companion object {

        private fun wrap(component: LatexStepSettingsComponent): JComponent {
//            val tooltip = "Select a step above to edit its type-level settings."
//            val panel = JPanel(BorderLayout())
//            panel.toolTipText = tooltip
// //            val label = JLabel("Step settings").apply {
// //                font = JBUI.Fonts.label().deriveFont(Font.BOLD)
// //                toolTipText = tooltip
// //            }
// //            component.toolTipText = tooltip
// //
// //            panel.add(label, BorderLayout.NORTH)
//            panel.add(component, BorderLayout.CENTER)
            return component
        }
    }
}
