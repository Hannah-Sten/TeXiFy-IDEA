package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBTextField
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.run.latex.ExternalToolStepOptions
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds

internal class ExternalToolStepFragmentedEditor(
    project: Project,
    initialStep: ExternalToolStepOptions = ExternalToolStepOptions(),
) : AbstractStepFragmentedEditor<ExternalToolStepOptions>(initialStep) {

    private val executable = JBTextField()
    private val executableRow = LabeledComponent.create(executable, TexifyBundle.message("run.step.ui.field.executable.label"))

    private val arguments = RawCommandLineEditor().apply {
        editorField.emptyText.text = TexifyBundle.message("run.step.ui.placeholder.custom.command.arguments")
    }
    private val argumentsRow = LabeledComponent.create(arguments, TexifyBundle.message("run.step.ui.field.arguments.label"))

    private val workingDirectory = createDirectoryField(project, TexifyBundle.message("run.step.ui.dialog.step.working.directory"))
    private val workingDirectoryRow = LabeledComponent.create(workingDirectory, TexifyBundle.message("run.step.ui.field.working.directory.label"))

    override fun createFragments(): Collection<SettingsEditorFragment<ExternalToolStepOptions, *>> {
        val header = CommonParameterFragments.createHeader<ExternalToolStepOptions>(TexifyBundle.message("run.step.ui.header.external.tool"))

        val executableFragment = stepFragment(
            id = StepUiOptionIds.COMMAND_EXECUTABLE,
            name = TexifyBundle.message("run.step.ui.field.executable"),
            component = executableRow,
            reset = { step, component -> component.component.text = step.executable.orEmpty() },
            apply = { step, component -> step.executable = component.component.text.ifBlank { null } },
            initiallyVisible = { true },
            removable = false,
            hint = TexifyBundle.message("run.step.ui.hint.external.tool.executable"),
        )

        val argsFragment = stepFragment(
            id = StepUiOptionIds.COMMAND_ARGS,
            name = TexifyBundle.message("run.step.ui.field.arguments"),
            component = argumentsRow,
            reset = { step, component -> component.component.text = step.arguments.orEmpty() },
            apply = { step, component -> step.arguments = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.arguments.isNullOrBlank() },
            removable = true,
            hint = TexifyBundle.message("run.step.ui.hint.external.tool.arguments"),
            actionHint = TexifyBundle.message("run.step.ui.action.set.external.tool.arguments"),
        )

        val workingDirFragment = stepFragment(
            id = StepUiOptionIds.STEP_WORKING_DIRECTORY,
            name = TexifyBundle.message("run.step.ui.field.working.directory"),
            component = workingDirectoryRow,
            reset = { step, component -> component.component.text = step.workingDirectoryPath.orEmpty() },
            apply = { step, component -> step.workingDirectoryPath = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.workingDirectoryPath.isNullOrBlank() },
            removable = true,
            hint = TexifyBundle.message("run.step.ui.hint.external.tool.working.directory"),
            actionHint = TexifyBundle.message("run.step.ui.action.set.step.working.directory"),
        )

        return listOf(
            header,
            executableFragment,
            argsFragment,
            workingDirFragment,
        )
    }
}
