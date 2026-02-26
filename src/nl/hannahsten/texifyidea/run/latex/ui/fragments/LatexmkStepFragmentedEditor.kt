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
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.ui.EditorTextField
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBTextField
import nl.hannahsten.texifyidea.run.compiler.LatexCompilePrograms
import nl.hannahsten.texifyidea.run.latex.LatexCommandLineOptionsCache
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds
import nl.hannahsten.texifyidea.run.latex.ui.LatexArgumentsCompletionProvider
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCitationTool
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import java.awt.event.ItemEvent

internal class LatexmkStepFragmentedEditor(
    private val project: Project,
    initialStep: LatexmkCompileStepOptions = LatexmkCompileStepOptions(),
) : AbstractStepFragmentedEditor<LatexmkCompileStepOptions>(initialStep) {

    private val compilerPath = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(true, false, false, false, false, false)
                    .withTitle("Choose Compiler Executable")
                    .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules.toSet().toTypedArray())
            )
        )
    }
    private val compilerPathRow = LabeledComponent.create(compilerPath, "Compiler path")

    private val compilerArguments = EditorTextField("", project, PlainTextFileType.INSTANCE).apply {
        setPlaceholder("Custom compiler arguments")
        setOneLineMode(true)
    }
    private val compilerArgumentsRow = LabeledComponent.create(compilerArguments, "Compiler arguments")
    private var completionInstalled = false

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
        ensureCompilerArgumentCompletionInstalled()
    }

    override fun createFragments(): Collection<SettingsEditorFragment<LatexmkCompileStepOptions, *>> {
        val headerFragment = CommonParameterFragments.createHeader<LatexmkCompileStepOptions>("Latexmk Step")

        val pathFragment = stepFragment(
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

        val argsFragment = stepFragment(
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

        val modeFragment = stepFragment(
            id = StepUiOptionIds.LATEXMK_MODE,
            name = "Latexmk compile mode",
            component = latexmkCompileModeRow,
            reset = { step, component -> component.component.selectedItem = step.latexmkCompileMode },
            apply = { step, component ->
                step.latexmkCompileMode = component.component.selectedItem as? LatexmkCompileMode ?: LatexmkCompileMode.AUTO
            },
            initiallyVisible = { true },
            removable = false,
            hint = "Latexmk compile mode used by latexmk-compile steps.",
        )

        val customEngineFragment = stepFragment(
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

        val citationFragment = stepFragment(
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

        val extraArgsFragment = stepFragment(
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
            modeFragment,
            pathFragment,
            argsFragment,
            customEngineFragment,
            citationFragment,
            extraArgsFragment,
        )
    }

    private fun ensureCompilerArgumentCompletionInstalled() {
        if (completionInstalled) {
            return
        }

        val options = LatexCommandLineOptionsCache.getOptionsOrFillCache(LatexCompilePrograms.LATEXMK_EXECUTABLE, project)
        LatexArgumentsCompletionProvider(options).apply(compilerArguments)
        completionInstalled = true
    }
}
