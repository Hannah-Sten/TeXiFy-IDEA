package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.FragmentedSettingsEditor
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.NlsContexts
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import java.util.function.BiConsumer
import java.util.function.Predicate
import javax.swing.JComponent

internal abstract class AbstractStepFragmentedEditor<TStep : LatexStepRunConfigurationOptions>(
    initialStep: TStep,
) : FragmentedSettingsEditor<TStep>(initialStep) {

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
    ): SettingsEditorFragment<TStep, C> {
        val fragment = SettingsEditorFragment<TStep, C>(
            id,
            name,
            null,
            component,
            0,
            BiConsumer { step, comp -> reset(step, comp) },
            BiConsumer { step, comp -> apply(step, comp) },
            Predicate { step -> initiallyVisible(step) }
        )
        fragment.isRemovable = removable
        fragment.isCanBeHidden = removable
        hint?.let { applyTooltip(component, it) }
        actionHint?.let { fragment.actionHint = it }
        return fragment
    }

    protected fun createDirectoryField(project: Project, title: String): TextFieldWithBrowseButton = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(false, true, false, false, false, false)
                    .withTitle(title)
                    .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules.toSet().toTypedArray())
            )
        )
    }

    protected fun applyTooltip(component: JComponent, tooltip: String) {
        component.toolTipText = tooltip
        if (component is LabeledComponent<*>) {
            component.component.toolTipText = tooltip
        }
    }
}
