package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBTextField
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds
import nl.hannahsten.texifyidea.run.latex.XindyStepOptions

internal class XindyStepFragmentedEditor(
    private val project: Project,
    initialStep: XindyStepOptions = XindyStepOptions(),
) : AbstractStepFragmentedEditor<XindyStepOptions>(initialStep) {

    private val executable = JBTextField()
    private val executableRow = LabeledComponent.create(executable, "Executable")

    private val arguments = RawCommandLineEditor().apply {
        editorField.emptyText.text = "Defaults to {main}.idx"
    }
    private val argumentsRow = LabeledComponent.create(arguments, "Arguments")

    private val workingDirectory = createDirectoryField(project, "Step working directory")
    private val workingDirectoryRow = LabeledComponent.create(workingDirectory, "Working directory")

    override fun createFragments(): Collection<SettingsEditorFragment<XindyStepOptions, *>> {
        val header = CommonParameterFragments.createHeader<XindyStepOptions>("Xindy Step")

        val executableFragment = stepFragment(
            id = StepUiOptionIds.COMMAND_EXECUTABLE,
            name = "Executable",
            component = executableRow,
            reset = { step, component -> component.component.text = step.executable.orEmpty() },
            apply = { step, component ->
                step.executable = component.component.text.ifBlank { "xindy" }
            },
            initiallyVisible = { true },
            removable = false,
            hint = "Xindy executable command.",
        )

        val argsFragment = stepFragment(
            id = StepUiOptionIds.COMMAND_ARGS,
            name = "Arguments",
            component = argumentsRow,
            reset = { step, component -> component.component.text = step.arguments.orEmpty() },
            apply = { step, component -> step.arguments = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.arguments.isNullOrBlank() },
            removable = true,
            hint = "Arguments passed to xindy executable.",
            actionHint = "Set xindy arguments",
        )

        val workingDirFragment = stepFragment(
            id = StepUiOptionIds.STEP_WORKING_DIRECTORY,
            name = "Working directory",
            component = workingDirectoryRow,
            reset = { step, component -> component.component.text = step.workingDirectoryPath.orEmpty() },
            apply = { step, component -> step.workingDirectoryPath = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.workingDirectoryPath.isNullOrBlank() },
            removable = true,
            hint = "Override working directory for this xindy step.",
            actionHint = "Set step working directory",
        )

        return listOf(
            header,
            executableFragment,
            argsFragment,
            workingDirFragment,
        )
    }
}
