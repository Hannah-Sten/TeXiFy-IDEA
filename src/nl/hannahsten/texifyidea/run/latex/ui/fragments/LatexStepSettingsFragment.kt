package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.ui.RunConfigurationEditorFragment
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import javax.swing.JComponent

internal class LatexStepSettingsFragment(
    private val component: LatexStepSettingsComponent,
    private val onChanged: () -> Unit,
) : RunConfigurationEditorFragment<LatexRunConfiguration, JComponent>(
    "stepSettings",
    "Step settings",
    null,
    component,
    0,
    { true }
) {

    init {
        component.changeListener = {
            onChanged()
            fireEditorStateChanged()
        }
        isRemovable = false
    }

    override fun doReset(s: RunnerAndConfigurationSettingsImpl) {
        component.resetEditorFrom()
    }

    override fun applyEditorTo(s: RunnerAndConfigurationSettingsImpl) {
        // Intentionally no-op. The platform may call fragment apply during dirty-state polling, so mutating the shared
        // shadow step list here would turn transient UI state into real selection-wide write-back. The real save path
        // is coordinated by LatexSettingsEditor.applyEditorTo(), which can distinguish live apply from snapshot apply.
    }
}
