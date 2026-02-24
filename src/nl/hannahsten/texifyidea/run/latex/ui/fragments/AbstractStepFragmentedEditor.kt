package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.FragmentedSettingsEditor
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.util.NlsContexts
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import java.util.function.BiConsumer
import java.util.function.Predicate
import javax.swing.JComponent

internal abstract class AbstractStepFragmentedEditor<TStep : LatexStepRunConfigurationOptions>(
    state: StepFragmentedState = StepFragmentedState(),
) : FragmentedSettingsEditor<StepFragmentedState>(state) {

    protected fun <C : JComponent> stepFragment(
        id: String,
        name: String,
        component: C,
        reset: (TStep, C) -> Unit,
        apply: (TStep, C) -> Unit,
        initiallyVisible: (TStep) -> Boolean,
        removable: Boolean,
        @NlsContexts.Tooltip hint: String? = null,
        actionHint: String? = null,
    ): SettingsEditorFragment<StepFragmentedState, C> {
        val fragment = SettingsEditorFragment<StepFragmentedState, C>(
            id,
            name,
            null,
            component,
            0,
            BiConsumer { state, comp ->
                withSelectedStep(state) { step -> reset(step, comp) }
            },
            BiConsumer { state, comp ->
                withSelectedStep(state) { step -> apply(step, comp) }
            },
            Predicate { state ->
                withSelectedStep(state) { step -> initiallyVisible(step) }
            }
        )
        fragment.isRemovable = removable
        fragment.isCanBeHidden = removable
        hint?.let { applyTooltip(component, it) }
        actionHint?.let { fragment.actionHint = it }
        return fragment
    }

    protected abstract fun selectedStep(state: StepFragmentedState): TStep

    protected fun applyTooltip(component: JComponent, tooltip: String) {
        component.toolTipText = tooltip
        if (component is LabeledComponent<*>) {
            component.component.toolTipText = tooltip
        }
    }

    private inline fun <R> withSelectedStep(state: StepFragmentedState, block: (TStep) -> R): R = block(selectedStep(state))
}
