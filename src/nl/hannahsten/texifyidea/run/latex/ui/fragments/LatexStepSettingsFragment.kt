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
    component,
    0,
    { true }
) {

    init {
        component.changeListener = { fireEditorStateChanged() }
        isRemovable = false
    }

    override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
        component.resetEditorFrom()
    }

    override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
        component.applyEditorTo()
    }
}
