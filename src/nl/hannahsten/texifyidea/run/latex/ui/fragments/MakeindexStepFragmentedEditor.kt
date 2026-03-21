package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBTextField
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.run.latex.MakeindexStepOptions
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds

internal class MakeindexStepFragmentedEditor(
    project: Project,
    initialStep: MakeindexStepOptions = MakeindexStepOptions(),
) : AbstractStepFragmentedEditor<MakeindexStepOptions>(initialStep) {

    private val program = ComboBox(MakeindexProgram.entries.toTypedArray())
    private val programRow = LabeledComponent.create(program, TexifyBundle.message("run.step.ui.field.program"))

    private val commandLineArguments = RawCommandLineEditor().apply {
        editorField.emptyText.text = TexifyBundle.message("run.step.ui.placeholder.custom.makeindex.arguments")
    }
    private val commandLineArgumentsRow = LabeledComponent.create(commandLineArguments, TexifyBundle.message("run.step.ui.field.program.arguments"))

    private val targetBaseName = JBTextField()
    private val targetBaseNameRow = LabeledComponent.create(targetBaseName, TexifyBundle.message("run.step.ui.field.target.base.name"))

    private val workingDirectory = createDirectoryField(project, TexifyBundle.message("run.step.ui.dialog.step.working.directory"))
    private val workingDirectoryRow = LabeledComponent.create(workingDirectory, TexifyBundle.message("run.step.ui.field.working.directory"))
    private var inferredWorkingDirectoryHint: String? = null

    fun setInferredWorkingDirectoryHint(value: String?) {
        inferredWorkingDirectoryHint = value
    }

    internal fun inferredWorkingDirectoryHintForTest(): String? = inferredWorkingDirectoryHint

    override fun createFragments(): Collection<SettingsEditorFragment<MakeindexStepOptions, *>> {
        val header = CommonParameterFragments.createHeader<MakeindexStepOptions>(TexifyBundle.message("run.step.ui.header.makeindex"))

        val programFragment = stepFragment(
            id = "step.makeindex.program",
            name = TexifyBundle.message("run.step.ui.field.program"),
            component = programRow,
            reset = { step, component -> component.component.selectedItem = step.program },
            apply = { step, component ->
                step.program = component.component.selectedItem as? MakeindexProgram ?: MakeindexProgram.MAKEINDEX
            },
            initiallyVisible = { true },
            removable = false,
            hint = TexifyBundle.message("run.step.ui.hint.makeindex.program"),
        )

        val argsFragment = stepFragment(
            id = StepUiOptionIds.MAKEINDEX_ARGS,
            name = TexifyBundle.message("run.step.ui.field.program.arguments"),
            component = commandLineArgumentsRow,
            reset = { step, component -> component.component.text = step.commandLineArguments.orEmpty() },
            apply = { step, component -> step.commandLineArguments = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.commandLineArguments.isNullOrBlank() },
            removable = true,
            hint = TexifyBundle.message("run.step.ui.hint.makeindex.arguments"),
            actionHint = TexifyBundle.message("run.step.ui.action.set.makeindex.arguments"),
        )

        val targetBaseFragment = stepFragment(
            id = StepUiOptionIds.MAKEINDEX_TARGET_BASE,
            name = TexifyBundle.message("run.step.ui.field.target.base.name"),
            component = targetBaseNameRow,
            reset = { step, component -> component.component.text = step.targetBaseNameOverride.orEmpty() },
            apply = { step, component -> step.targetBaseNameOverride = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.targetBaseNameOverride.isNullOrBlank() },
            removable = true,
            hint = TexifyBundle.message("run.step.ui.hint.makeindex.target.base.name"),
            actionHint = TexifyBundle.message("run.step.ui.action.set.target.base.name"),
        )

        val workingDirFragment = stepWorkingDirectoryFragment(
            component = workingDirectoryRow,
            inferredWorkingDirectoryHint = { inferredWorkingDirectoryHint },
            getWorkingDirectoryPath = { it.workingDirectoryPath },
            setWorkingDirectoryPath = { step, value -> step.workingDirectoryPath = value },
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
