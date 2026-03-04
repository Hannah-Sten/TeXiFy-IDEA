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
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.latex.BibtexStepOptions
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds

internal class BibtexStepFragmentedEditor(
    private val project: Project,
    initialStep: BibtexStepOptions = BibtexStepOptions(),
) : AbstractStepFragmentedEditor<BibtexStepOptions>(initialStep) {

    private val compiler = ComboBox(BibliographyCompiler.entries.toTypedArray())
    private val compilerRow = LabeledComponent.create(compiler, "Bibliography tool")

    private val compilerPath = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(true, false, false, false, false, false)
                    .withTitle("Choose Bibliography Executable")
                    .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules.toSet().toTypedArray())
            )
        )
    }
    private val compilerPathRow = LabeledComponent.create(compilerPath, "Compiler path")

    private val compilerArguments = RawCommandLineEditor().apply {
        editorField.emptyText.text = "Custom bibliography arguments"
    }
    private val compilerArgumentsRow = LabeledComponent.create(compilerArguments, "Compiler arguments")

    private val workingDirectory = createDirectoryField(project, "Step working directory")
    private val workingDirectoryRow = LabeledComponent.create(workingDirectory, "Working directory")

    override fun createFragments(): Collection<SettingsEditorFragment<BibtexStepOptions, *>> {
        val header = CommonParameterFragments.createHeader<BibtexStepOptions>("Bibliography Step")

        val compilerFragment = stepFragment(
            id = "step.bib.compiler",
            name = "Bibliography tool",
            component = compilerRow,
            reset = { step, component -> component.component.selectedItem = step.bibliographyCompiler },
            apply = { step, component ->
                step.bibliographyCompiler = component.component.selectedItem as? BibliographyCompiler ?: BibliographyCompiler.BIBTEX
            },
            initiallyVisible = { true },
            removable = false,
            hint = "Bibliography compiler used by this step.",
        )

        val pathFragment = stepFragment(
            id = StepUiOptionIds.BIB_COMPILER_PATH,
            name = "Compiler path",
            component = compilerPathRow,
            reset = { step, component -> component.component.text = step.compilerPath.orEmpty() },
            apply = { step, component -> step.compilerPath = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.compilerPath.isNullOrBlank() },
            removable = true,
            hint = "Optional absolute path to bibliography executable.",
            actionHint = "Set bibliography executable path",
        )

        val argsFragment = stepFragment(
            id = StepUiOptionIds.BIB_COMPILER_ARGS,
            name = "Compiler arguments",
            component = compilerArgumentsRow,
            reset = { step, component -> component.component.text = step.compilerArguments.orEmpty() },
            apply = { step, component -> step.compilerArguments = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.compilerArguments.isNullOrBlank() },
            removable = true,
            hint = "Additional arguments passed to bibliography compiler.",
            actionHint = "Set bibliography arguments",
        )

        val workingDirFragment = stepFragment(
            id = StepUiOptionIds.STEP_WORKING_DIRECTORY,
            name = "Working directory",
            component = workingDirectoryRow,
            reset = { step, component -> component.component.text = step.workingDirectoryPath.orEmpty() },
            apply = { step, component -> step.workingDirectoryPath = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.workingDirectoryPath.isNullOrBlank() },
            removable = true,
            hint = "Override working directory for this bibliography step.",
            actionHint = "Set step working directory",
        )

        return listOf(
            header,
            compilerFragment,
            pathFragment,
            argsFragment,
            workingDirFragment,
        )
    }
}
