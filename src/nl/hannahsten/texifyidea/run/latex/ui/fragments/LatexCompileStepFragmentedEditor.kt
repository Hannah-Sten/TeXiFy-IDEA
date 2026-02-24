package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.RawCommandLineEditor
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.latex.LatexCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds
import java.awt.event.ItemEvent

internal class LatexCompileStepFragmentedEditor(
    private val project: Project,
    state: StepFragmentedState = StepFragmentedState(),
) : AbstractStepFragmentedEditor<LatexCompileStepOptions>(state) {

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

    init {
        compiler.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED) {
                syncOutputFormatOptions(it.item as? LatexCompiler ?: LatexCompiler.PDFLATEX)
                fireEditorStateChanged()
            }
        }
    }

    override fun createFragments(): Collection<SettingsEditorFragment<StepFragmentedState, *>> {
        val headerFragment = CommonParameterFragments.createHeader<StepFragmentedState>("LaTeX compile step")

        val compilerFragment = stepFragment(
            id = "step.compile.compiler",
            name = "Compiler",
            component = compilerRow,
            reset = { step, component ->
                component.component.selectedItem = step.compiler.takeIf { it != LatexCompiler.LATEXMK } ?: LatexCompiler.PDFLATEX
            },
            apply = { step, component ->
                val selectedCompiler = component.component.selectedItem as? LatexCompiler ?: LatexCompiler.PDFLATEX
                step.compiler = selectedCompiler
            },
            initiallyVisible = { true },
            removable = false,
            hint = "Compiler used by latex-compile step type.",
        )

        val pathFragment = stepFragment(
            id = StepUiOptionIds.COMPILE_PATH,
            name = "Compiler path",
            component = compilerPathRow,
            reset = { step, component -> component.component.text = step.compilerPath.orEmpty() },
            apply = { step, component -> step.compilerPath = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.compilerPath.isNullOrBlank() },
            removable = true,
            hint = "Compiler executable used by latex-compile steps.",
            actionHint = "Set custom compiler path",
        )

        val argsFragment = stepFragment(
            id = StepUiOptionIds.COMPILE_ARGS,
            name = "Compiler arguments",
            component = compilerArgumentsRow,
            reset = { step, component -> component.component.text = step.compilerArguments.orEmpty() },
            apply = { step, component -> step.compilerArguments = component.component.text },
            initiallyVisible = { step -> !step.compilerArguments.isNullOrBlank() },
            removable = true,
            hint = "Arguments passed to the selected compiler.",
            actionHint = "Set custom compiler arguments",
        )

        val formatFragment = stepFragment(
            id = StepUiOptionIds.COMPILE_OUTPUT_FORMAT,
            name = "Output format",
            component = outputFormatRow,
            reset = { step, component ->
                val selectedCompiler = compiler.selectedItem as? LatexCompiler ?: LatexCompiler.PDFLATEX
                syncOutputFormatOptions(selectedCompiler, step.outputFormat)
                component.component.selectedItem = step.outputFormat
            },
            apply = { step, component ->
                step.outputFormat = component.component.selectedItem as? Format ?: Format.PDF
            },
            initiallyVisible = { step -> step.outputFormat != Format.PDF },
            removable = true,
            hint = "Output format used by latex-compile steps.",
            actionHint = "Set output format",
        )

        return listOf(
            headerFragment,
            compilerFragment,
            pathFragment,
            argsFragment,
            formatFragment,
        )
    }

    private fun syncOutputFormatOptions(selectedCompiler: LatexCompiler, preferred: Format? = null) {
        val supportedFormats = selectedCompiler.outputFormats
        outputFormat.removeAllItems()
        supportedFormats.forEach(outputFormat::addItem)
        outputFormat.selectedItem = preferred.takeIf { supportedFormats.contains(it) } ?: supportedFormats.firstOrNull() ?: Format.PDF
    }

    override fun selectedStep(state: StepFragmentedState): LatexCompileStepOptions = state.selectedStepOptions as? LatexCompileStepOptions
        ?: LatexCompileStepOptions().also { state.selectedStepOptions = it }
}
