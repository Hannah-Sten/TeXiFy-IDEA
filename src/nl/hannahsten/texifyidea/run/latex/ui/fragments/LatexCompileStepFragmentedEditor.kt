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
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds
import nl.hannahsten.texifyidea.run.latex.ui.LatexDistributionComboBoxRenderer
import nl.hannahsten.texifyidea.run.latex.ui.LatexDistributionSelection
import java.awt.event.ItemEvent
import java.util.function.BiConsumer
import java.util.function.Predicate
import javax.swing.JComponent

internal class LatexCompileStepFragmentedEditor(
    private val project: Project,
    state: StepFragmentedState = StepFragmentedState(),
) : FragmentedSettingsEditor<StepFragmentedState>(state) {

    private val compiler = ComboBox(LatexCompiler.entries.filter { it != LatexCompiler.LATEXMK }.toTypedArray())
    private val compilerRow = LabeledComponent.create(compiler, "Compiler")

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

    private val outputFormat = ComboBox(Format.entries.toTypedArray())
    private val outputFormatRow = LabeledComponent.create(outputFormat, "Output format")

    private val latexDistribution = ComboBox(LatexDistributionSelection.getAvailableSelections(project).toTypedArray()).apply {
        renderer = LatexDistributionComboBoxRenderer(project) { null }
    }
    private val latexDistributionRow = LabeledComponent.create(latexDistribution, "LaTeX distribution")

    init {
        compiler.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED) {
                syncOutputFormatOptions(it.item as? LatexCompiler ?: LatexCompiler.PDFLATEX)
                fireEditorStateChanged()
            }
        }
    }

    override fun createFragments(): Collection<SettingsEditorFragment<StepFragmentedState, *>> {
        val compilerFragment = fragment(
            id = "step.compile.compiler",
            name = "Compiler",
            component = compilerRow,
            reset = { runConfig, component ->
                component.component.selectedItem = runConfig.compiler?.takeIf { it != LatexCompiler.LATEXMK } ?: LatexCompiler.PDFLATEX
            },
            apply = { runConfig, component ->
                val selectedCompiler = component.component.selectedItem as? LatexCompiler ?: LatexCompiler.PDFLATEX
                runConfig.compiler = selectedCompiler
                if (selectedCompiler.includesBibtex) {
                    runConfig.bibRunConfigs = setOf()
                }
                if (selectedCompiler.includesMakeindex) {
                    runConfig.makeindexRunConfigs = setOf()
                }
            },
            initiallyVisible = { true },
            removable = false,
            hint = "Compiler used by latex-compile step type.",
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

        val formatFragment = fragment(
            id = StepUiOptionIds.COMPILE_OUTPUT_FORMAT,
            name = "Output format",
            component = outputFormatRow,
            reset = { runConfig, component ->
                val selectedCompiler = compiler.selectedItem as? LatexCompiler ?: LatexCompiler.PDFLATEX
                syncOutputFormatOptions(selectedCompiler, runConfig.outputFormat)
                component.component.selectedItem = runConfig.outputFormat
            },
            apply = { runConfig, component ->
                runConfig.outputFormat = component.component.selectedItem as? Format ?: Format.PDF
            },
            initiallyVisible = { runConfig -> runConfig.outputFormat != Format.PDF },
            removable = true,
            actionHint = "Set output format",
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

        return listOf(
            compilerFragment,
            pathFragment,
            argsFragment,
            formatFragment,
            distributionFragment,
        )
    }

    private fun syncOutputFormatOptions(selectedCompiler: LatexCompiler, preferred: Format? = null) {
        val supportedFormats = selectedCompiler.outputFormats
        outputFormat.removeAllItems()
        supportedFormats.forEach(outputFormat::addItem)
        outputFormat.selectedItem = preferred.takeIf { supportedFormats.contains(it) } ?: supportedFormats.firstOrNull() ?: Format.PDF
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
