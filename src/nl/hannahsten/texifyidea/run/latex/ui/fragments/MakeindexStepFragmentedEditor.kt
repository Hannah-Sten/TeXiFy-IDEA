package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBTextField
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.run.latex.MakeindexStepOptions
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds

internal class MakeindexStepFragmentedEditor(
    private val project: Project,
    initialStep: MakeindexStepOptions = MakeindexStepOptions(),
) : AbstractStepFragmentedEditor<MakeindexStepOptions>(initialStep) {

    private val program = ComboBox(MakeindexProgram.entries.toTypedArray())
    private val programRow = LabeledComponent.create(program, "Program")

    private val commandLineArguments = RawCommandLineEditor().apply {
        editorField.emptyText.text = "Custom makeindex arguments"
    }
    private val commandLineArgumentsRow = LabeledComponent.create(commandLineArguments, "Program arguments")

    private val targetBaseName = JBTextField()
    private val targetBaseNameRow = LabeledComponent.create(targetBaseName, "Target base name")

    private val workingDirectory = createDirectoryField(project, "Step working directory")
    private val workingDirectoryRow = LabeledComponent.create(workingDirectory, "Working directory")

    override fun createFragments(): Collection<SettingsEditorFragment<MakeindexStepOptions, *>> {
        val header = CommonParameterFragments.createHeader<MakeindexStepOptions>("Makeindex step")

        val programFragment = stepFragment(
            id = "step.makeindex.program",
            name = "Program",
            component = programRow,
            reset = { step, component -> component.component.selectedItem = step.program },
            apply = { step, component ->
                step.program = component.component.selectedItem as? MakeindexProgram ?: MakeindexProgram.MAKEINDEX
            },
            initiallyVisible = { true },
            removable = false,
            hint = "Index program used by this step.",
        )

        val argsFragment = stepFragment(
            id = StepUiOptionIds.MAKEINDEX_ARGS,
            name = "Program arguments",
            component = commandLineArgumentsRow,
            reset = { step, component -> component.component.text = step.commandLineArguments.orEmpty() },
            apply = { step, component -> step.commandLineArguments = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.commandLineArguments.isNullOrBlank() },
            removable = true,
            hint = "Additional arguments passed to makeindex program.",
            actionHint = "Set makeindex arguments",
        )

        val targetBaseFragment = stepFragment(
            id = StepUiOptionIds.MAKEINDEX_TARGET_BASE,
            name = "Target base name",
            component = targetBaseNameRow,
            reset = { step, component -> component.component.text = step.targetBaseNameOverride.orEmpty() },
            apply = { step, component -> step.targetBaseNameOverride = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.targetBaseNameOverride.isNullOrBlank() },
            removable = true,
            hint = "Override basename of generated index files.",
            actionHint = "Set target base name",
        )

        val workingDirFragment = stepFragment(
            id = StepUiOptionIds.STEP_WORKING_DIRECTORY,
            name = "Working directory",
            component = workingDirectoryRow,
            reset = { step, component -> component.component.text = step.workingDirectoryPath.orEmpty() },
            apply = { step, component -> step.workingDirectoryPath = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.workingDirectoryPath.isNullOrBlank() },
            removable = true,
            hint = "Override working directory for this makeindex step.",
            actionHint = "Set step working directory",
        )

        return listOf(
            header,
            programFragment,
            argsFragment,
            targetBaseFragment,
            workingDirFragment,
        )
    }
}
