package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.FragmentedSettingsEditor
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBTextField
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCitationTool
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import java.awt.event.ItemEvent
import java.util.function.BiConsumer
import java.util.function.Predicate
import javax.swing.JComponent
import javax.swing.JLabel

internal class LatexmkStepFragmentedEditor(
    private val project: Project,
    state: StepFragmentedState = StepFragmentedState(),
) : FragmentedSettingsEditor<StepFragmentedState>(state) {

    private val compilerRow = LabeledComponent.create(JLabel("latexmk"), "Compiler")

    private val compilerPath = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(true, false, false, false, false, false)
                    .withTitle("Choose compiler executable")
                    .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules.toSet().toTypedArray())
            )
        )
    }
    private val compilerPathRow = LabeledComponent.create(compilerPath, "Compiler path")

    private val compilerArguments = RawCommandLineEditor().apply {
        editorField.emptyText.text = "Custom compiler arguments"
    }
    private val compilerArgumentsRow = LabeledComponent.create(compilerArguments, "Compiler arguments")

    private val latexmkCompileMode = ComboBox(LatexmkCompileMode.entries.toTypedArray())
    private val latexmkCompileModeRow = LabeledComponent.create(latexmkCompileMode, "Latexmk compile mode")

    private val latexmkCustomEngineCommand = JBTextField()
    private val latexmkCustomEngineRow = LabeledComponent.create(latexmkCustomEngineCommand, "Latexmk custom engine command")

    private val latexmkCitationTool = ComboBox(LatexmkCitationTool.entries.toTypedArray())
    private val latexmkCitationToolRow = LabeledComponent.create(latexmkCitationTool, "Latexmk citation tool")

    private val latexmkExtraArguments = RawCommandLineEditor().apply {
        editorField.emptyText.text = "Additional latexmk arguments"
    }
    private val latexmkExtraArgumentsRow = LabeledComponent.create(latexmkExtraArguments, "Latexmk extra arguments")

    init {
        latexmkCompileMode.addItemListener {
            if (it.stateChange != ItemEvent.SELECTED) {
                return@addItemListener
            }
            latexmkCustomEngineCommand.isEnabled = latexmkCompileMode.selectedItem == LatexmkCompileMode.CUSTOM
            if (!latexmkCustomEngineCommand.isEnabled) {
                latexmkCustomEngineCommand.text = ""
            }
            fireEditorStateChanged()
        }
    }

    override fun createFragments(): Collection<SettingsEditorFragment<StepFragmentedState, *>> {
        val headerFragment = CommonParameterFragments.createHeader<StepFragmentedState>("latexmk step")

        val compilerFragment = fragment(
            id = "step.latexmk.compiler",
            name = "Compiler",
            component = compilerRow,
            reset = { _, _ -> },
            apply = { _, _ -> },
            initiallyVisible = { true },
            removable = false,
            hint = "Compiler used by latexmk-compile step type.",
        )

        val pathFragment = fragment(
            id = StepUiOptionIds.COMPILE_PATH,
            name = "Compiler path",
            component = compilerPathRow,
            reset = { step, component -> component.component.text = step.compilerPath.orEmpty() },
            apply = { step, component -> step.compilerPath = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.compilerPath.isNullOrBlank() },
            removable = true,
            hint = "Compiler executable used by latexmk-compile steps.",
            actionHint = "Set custom compiler path",
        )

        val argsFragment = fragment(
            id = StepUiOptionIds.COMPILE_ARGS,
            name = "Compiler arguments",
            component = compilerArgumentsRow,
            reset = { step, component -> component.component.text = step.compilerArguments.orEmpty() },
            apply = { step, component -> step.compilerArguments = component.component.text },
            initiallyVisible = { step -> !step.compilerArguments.isNullOrBlank() },
            removable = true,
            hint = "Arguments passed to latexmk.",
            actionHint = "Set custom compiler arguments",
        )

        val modeFragment = fragment(
            id = StepUiOptionIds.LATEXMK_MODE,
            name = "Latexmk compile mode",
            component = latexmkCompileModeRow,
            reset = { step, component -> component.component.selectedItem = step.latexmkCompileMode },
            apply = { step, component ->
                step.latexmkCompileMode = component.component.selectedItem as? LatexmkCompileMode ?: LatexmkCompileMode.AUTO
            },
            initiallyVisible = { step -> step.latexmkCompileMode != LatexmkCompileMode.AUTO },
            removable = true,
            hint = "latexmk compile mode used by latexmk-compile steps.",
            actionHint = "Set latexmk compile mode",
        )

        val customEngineFragment = fragment(
            id = StepUiOptionIds.LATEXMK_CUSTOM_ENGINE,
            name = "Latexmk custom engine command",
            component = latexmkCustomEngineRow,
            reset = { step, component ->
                component.component.text = step.latexmkCustomEngineCommand.orEmpty()
                component.component.isEnabled = step.latexmkCompileMode == LatexmkCompileMode.CUSTOM
            },
            apply = { step, component ->
                step.latexmkCustomEngineCommand = component.component.text
            },
            initiallyVisible = { step ->
                step.latexmkCompileMode == LatexmkCompileMode.CUSTOM || !step.latexmkCustomEngineCommand.isNullOrBlank()
            },
            removable = true,
            hint = "Custom engine command used when latexmk mode is CUSTOM.",
            actionHint = "Set latexmk custom engine command",
        )

        val citationFragment = fragment(
            id = StepUiOptionIds.LATEXMK_CITATION,
            name = "Latexmk citation tool",
            component = latexmkCitationToolRow,
            reset = { step, component -> component.component.selectedItem = step.latexmkCitationTool },
            apply = { step, component ->
                step.latexmkCitationTool = component.component.selectedItem as? LatexmkCitationTool ?: LatexmkCitationTool.AUTO
            },
            initiallyVisible = { step -> step.latexmkCitationTool != LatexmkCitationTool.AUTO },
            removable = true,
            hint = "Citation tool used by latexmk.",
            actionHint = "Set latexmk citation tool",
        )

        val extraArgsFragment = fragment(
            id = StepUiOptionIds.LATEXMK_EXTRA_ARGS,
            name = "Latexmk extra arguments",
            component = latexmkExtraArgumentsRow,
            reset = { step, component -> component.component.text = step.latexmkExtraArguments.orEmpty() },
            apply = { step, component -> step.latexmkExtraArguments = component.component.text },
            initiallyVisible = { step ->
                !step.latexmkExtraArguments.isNullOrBlank() &&
                    step.latexmkExtraArguments != LatexRunConfiguration.DEFAULT_LATEXMK_EXTRA_ARGUMENTS
            },
            removable = true,
            hint = "Additional arguments appended to latexmk invocation.",
            actionHint = "Set latexmk extra arguments",
        )

        return listOf(
            headerFragment,
            compilerFragment,
            pathFragment,
            argsFragment,
            modeFragment,
            customEngineFragment,
            citationFragment,
            extraArgsFragment,
        )
    }

    private fun <C : JComponent> fragment(
        id: String,
        name: String,
        component: C,
        reset: (LatexmkCompileStepOptions, C) -> Unit,
        apply: (LatexmkCompileStepOptions, C) -> Unit,
        initiallyVisible: (LatexmkCompileStepOptions) -> Boolean,
        removable: Boolean,
        @NlsContexts.Tooltip hint: String? = null,
        actionHint: String? = null,
    ): SettingsEditorFragment<StepFragmentedState, C> {
        val fragment = SettingsEditorFragment(
            id,
            name,
            null,
            component,
            0,
            BiConsumer<StepFragmentedState, C> { state, comp ->
                withSelectedStep(state) { runConfig -> reset(runConfig, comp) }
            },
            BiConsumer<StepFragmentedState, C> { state, comp ->
                withSelectedStep(state) { runConfig -> apply(runConfig, comp) }
            },
            Predicate<StepFragmentedState> { state ->
                withSelectedStep(state) { runConfig -> initiallyVisible(runConfig) }
            }
        )
        fragment.isRemovable = removable
        fragment.isCanBeHidden = removable
        hint?.let { applyTooltip(component, it) }
        actionHint?.let { fragment.actionHint = it }
        return fragment
    }

    private fun applyTooltip(component: JComponent, tooltip: String) {
        component.toolTipText = tooltip
        if (component is LabeledComponent<*>) {
            component.component.toolTipText = tooltip
        }
    }

    private inline fun <T> withSelectedStep(state: StepFragmentedState, block: (LatexmkCompileStepOptions) -> T): T {
        val step = state.selectedStepOptions as? LatexmkCompileStepOptions
            ?: LatexmkCompileStepOptions().also { state.selectedStepOptions = it }
        return block(step)
    }
}
