package nl.hannahsten.texifyidea.run.latex.ui.fragments

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
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds
import nl.hannahsten.texifyidea.run.latex.ui.LatexDistributionComboBoxRenderer
import nl.hannahsten.texifyidea.run.latex.ui.LatexDistributionSelection
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

    private val latexDistribution = ComboBox(LatexDistributionSelection.getAvailableSelections(project).toTypedArray()).apply {
        renderer = LatexDistributionComboBoxRenderer(project) { null }
    }
    private val latexDistributionRow = LabeledComponent.create(latexDistribution, "LaTeX distribution")

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
        val compilerFragment = fragment(
            id = "step.latexmk.compiler",
            name = "Compiler",
            component = compilerRow,
            reset = { _, _ -> },
            apply = { runConfig, _ ->
                runConfig.compiler = LatexCompiler.LATEXMK
                runConfig.bibRunConfigs = setOf()
                runConfig.makeindexRunConfigs = setOf()
                runConfig.externalToolRunConfigs = setOf()
            },
            initiallyVisible = { true },
            removable = false,
            hint = "Compiler used by latexmk-compile step type.",
        )

        val pathFragment = fragment(
            id = StepUiOptionIds.COMPILE_PATH,
            name = "Compiler path",
            component = compilerPathRow,
            reset = { runConfig, component -> component.component.text = runConfig.compilerPath.orEmpty() },
            apply = { runConfig, component -> runConfig.compilerPath = component.component.text.ifBlank { null } },
            initiallyVisible = { runConfig -> !runConfig.compilerPath.isNullOrBlank() },
            removable = true,
            actionHint = "Set custom compiler path",
        )

        val argsFragment = fragment(
            id = StepUiOptionIds.COMPILE_ARGS,
            name = "Compiler arguments",
            component = compilerArgumentsRow,
            reset = { runConfig, component -> component.component.text = runConfig.compilerArguments.orEmpty() },
            apply = { runConfig, component -> runConfig.compilerArguments = component.component.text },
            initiallyVisible = { runConfig -> !runConfig.compilerArguments.isNullOrBlank() },
            removable = true,
            actionHint = "Set custom compiler arguments",
        )

        val distributionFragment = fragment(
            id = StepUiOptionIds.COMPILE_DISTRIBUTION,
            name = "LaTeX distribution",
            component = latexDistributionRow,
            reset = { runConfig, component ->
                refreshDistributionSelections(runConfig.latexDistribution)
                component.component.selectedItem = LatexDistributionSelection.getAvailableSelections(project)
                    .firstOrNull { it.distributionType == runConfig.latexDistribution }
                    ?: LatexDistributionSelection.fromDistributionType(runConfig.latexDistribution)
            },
            apply = { runConfig, component ->
                val selected = component.component.selectedItem as? LatexDistributionSelection
                runConfig.latexDistribution = selected?.distributionType ?: LatexDistributionType.MODULE_SDK
            },
            initiallyVisible = { runConfig -> runConfig.latexDistribution != LatexDistributionType.MODULE_SDK },
            removable = true,
            actionHint = "Set LaTeX distribution",
        )

        val modeFragment = fragment(
            id = StepUiOptionIds.LATEXMK_MODE,
            name = "Latexmk compile mode",
            component = latexmkCompileModeRow,
            reset = { runConfig, component -> component.component.selectedItem = runConfig.latexmkCompileMode },
            apply = { runConfig, component ->
                runConfig.latexmkCompileMode = component.component.selectedItem as? LatexmkCompileMode ?: LatexmkCompileMode.AUTO
            },
            initiallyVisible = { runConfig -> runConfig.latexmkCompileMode != LatexmkCompileMode.AUTO },
            removable = true,
            actionHint = "Set latexmk compile mode",
        )

        val customEngineFragment = fragment(
            id = StepUiOptionIds.LATEXMK_CUSTOM_ENGINE,
            name = "Latexmk custom engine command",
            component = latexmkCustomEngineRow,
            reset = { runConfig, component ->
                component.component.text = runConfig.latexmkCustomEngineCommand.orEmpty()
                component.component.isEnabled = runConfig.latexmkCompileMode == LatexmkCompileMode.CUSTOM
            },
            apply = { runConfig, component ->
                runConfig.latexmkCustomEngineCommand = component.component.text
            },
            initiallyVisible = { runConfig ->
                runConfig.latexmkCompileMode == LatexmkCompileMode.CUSTOM || !runConfig.latexmkCustomEngineCommand.isNullOrBlank()
            },
            removable = true,
            actionHint = "Set latexmk custom engine command",
        )

        val citationFragment = fragment(
            id = StepUiOptionIds.LATEXMK_CITATION,
            name = "Latexmk citation tool",
            component = latexmkCitationToolRow,
            reset = { runConfig, component -> component.component.selectedItem = runConfig.latexmkCitationTool },
            apply = { runConfig, component ->
                runConfig.latexmkCitationTool = component.component.selectedItem as? LatexmkCitationTool ?: LatexmkCitationTool.AUTO
            },
            initiallyVisible = { runConfig -> runConfig.latexmkCitationTool != LatexmkCitationTool.AUTO },
            removable = true,
            actionHint = "Set latexmk citation tool",
        )

        val extraArgsFragment = fragment(
            id = StepUiOptionIds.LATEXMK_EXTRA_ARGS,
            name = "Latexmk extra arguments",
            component = latexmkExtraArgumentsRow,
            reset = { runConfig, component -> component.component.text = runConfig.latexmkExtraArguments.orEmpty() },
            apply = { runConfig, component -> runConfig.latexmkExtraArguments = component.component.text },
            initiallyVisible = { runConfig ->
                !runConfig.latexmkExtraArguments.isNullOrBlank() &&
                    runConfig.latexmkExtraArguments != LatexRunConfiguration.DEFAULT_LATEXMK_EXTRA_ARGUMENTS
            },
            removable = true,
            actionHint = "Set latexmk extra arguments",
        )

        return listOf(
            compilerFragment,
            pathFragment,
            argsFragment,
            distributionFragment,
            modeFragment,
            customEngineFragment,
            citationFragment,
            extraArgsFragment,
        )
    }

    private fun refreshDistributionSelections(selected: LatexDistributionType) {
        val options = LatexDistributionSelection.getAvailableSelections(project).toMutableList()
        if (options.none { it.distributionType == selected }) {
            options += LatexDistributionSelection.fromDistributionType(selected)
        }
        latexDistribution.removeAllItems()
        options.forEach(latexDistribution::addItem)
    }

    private fun <C : JComponent> fragment(
        id: String,
        name: String,
        component: C,
        reset: (LatexRunConfiguration, C) -> Unit,
        apply: (LatexRunConfiguration, C) -> Unit,
        initiallyVisible: (LatexRunConfiguration) -> Boolean,
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
            BiConsumer<StepFragmentedState, C> { state, comp -> reset(state.runConfig, comp) },
            BiConsumer<StepFragmentedState, C> { state, comp -> apply(state.runConfig, comp) },
            Predicate<StepFragmentedState> { state -> initiallyVisible(state.runConfig) }
        )
        fragment.isRemovable = removable
        fragment.isCanBeHidden = removable
        hint?.let(fragment::setHint)
        actionHint?.let { fragment.actionHint = it }
        return fragment
    }
}
