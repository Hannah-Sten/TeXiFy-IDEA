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
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.latex.LatexCommandLineOptionsCache
import nl.hannahsten.texifyidea.run.latex.LatexCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds
import nl.hannahsten.texifyidea.run.latex.ui.LatexArgumentsCompletionProvider
import java.awt.event.ItemEvent

internal class LatexCompileStepFragmentedEditor(
    private val project: Project,
    initialStep: LatexCompileStepOptions = LatexCompileStepOptions(),
) : AbstractStepFragmentedEditor<LatexCompileStepOptions>(initialStep) {

    private val compiler = ComboBox(LatexCompiler.entries.toTypedArray())
    private val compilerRow = LabeledComponent.create(compiler, TexifyBundle.message("run.step.ui.field.compiler.label"))

    private val compilerPath = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(true, false, false, false, false, false)
                    .withTitle(TexifyBundle.message("run.step.ui.dialog.choose.compiler.executable"))
                    .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules.toSet().toTypedArray())
            )
        )
    }
    private val compilerPathRow = LabeledComponent.create(compilerPath, TexifyBundle.message("run.step.ui.field.compiler.path.label"))

    private val compilerArguments = EditorTextField("", project, PlainTextFileType.INSTANCE).apply {
        setPlaceholder(TexifyBundle.message("run.latex.settings.custom.compiler.arguments"))
        @Suppress("UsePropertyAccessSyntax")
        setOneLineMode(true)
    }
    private val compilerArgumentsRow = LabeledComponent.create(compilerArguments, TexifyBundle.message("run.step.ui.field.compiler.arguments.label"))
    private var completionCompilerExecutable: String? = null

    private val outputFormat = ComboBox(Format.entries.toTypedArray())
    private val outputFormatRow = LabeledComponent.create(outputFormat, TexifyBundle.message("run.step.ui.field.output.format.label"))

    init {
        compiler.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED) {
                val selectedCompiler = it.item as? LatexCompiler ?: LatexCompiler.PDFLATEX
                syncOutputFormatOptions(selectedCompiler)
                syncCompilerArgumentCompletion(selectedCompiler)
                fireEditorStateChanged()
            }
        }
        syncCompilerArgumentCompletion(compiler.selectedItem as? LatexCompiler ?: LatexCompiler.PDFLATEX)
    }

    override fun createFragments(): Collection<SettingsEditorFragment<LatexCompileStepOptions, *>> {
        val headerFragment = CommonParameterFragments.createHeader<LatexCompileStepOptions>(TexifyBundle.message("run.step.ui.header.latex.compile"))

        val compilerFragment = stepFragment(
            id = "step.compile.compiler",
            name = TexifyBundle.message("run.step.ui.field.compiler"),
            component = compilerRow,
            reset = { step, component ->
                component.component.selectedItem = step.compiler
                syncCompilerArgumentCompletion(step.compiler)
            },
            apply = { step, component ->
                val selectedCompiler = component.component.selectedItem as? LatexCompiler ?: LatexCompiler.PDFLATEX
                step.compiler = selectedCompiler
            },
            initiallyVisible = { true },
            removable = false,
            hint = TexifyBundle.message("run.step.ui.hint.compile.compiler"),
        )

        val pathFragment = stepFragment(
            id = StepUiOptionIds.COMPILE_PATH,
            name = TexifyBundle.message("run.step.ui.field.compiler.path"),
            component = compilerPathRow,
            reset = { step, component -> component.component.text = step.compilerPath.orEmpty() },
            apply = { step, component -> step.compilerPath = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.compilerPath.isNullOrBlank() },
            removable = true,
            hint = TexifyBundle.message("run.step.ui.hint.compile.compiler.path"),
            actionHint = TexifyBundle.message("run.step.ui.action.set.custom.compiler.path"),
        )

        val argsFragment = stepFragment(
            id = StepUiOptionIds.COMPILE_ARGS,
            name = TexifyBundle.message("run.step.ui.field.compiler.arguments"),
            component = compilerArgumentsRow,
            reset = { step, component -> component.component.text = step.compilerArguments.orEmpty() },
            apply = { step, component -> step.compilerArguments = component.component.text },
            initiallyVisible = { step -> !step.compilerArguments.isNullOrBlank() },
            removable = true,
            hint = TexifyBundle.message("run.step.ui.hint.compile.compiler.arguments"),
            actionHint = TexifyBundle.message("run.step.ui.action.set.custom.compiler.arguments"),
        )

        val formatFragment = stepFragment(
            id = StepUiOptionIds.COMPILE_OUTPUT_FORMAT,
            name = TexifyBundle.message("run.step.ui.field.output.format"),
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
            hint = TexifyBundle.message("run.step.ui.hint.compile.output.format"),
            actionHint = TexifyBundle.message("run.step.ui.action.set.output.format"),
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

    private fun syncCompilerArgumentCompletion(selectedCompiler: LatexCompiler) {
        val executable = selectedCompiler.executableName
        if (completionCompilerExecutable == executable) {
            return
        }

        val options = LatexCommandLineOptionsCache.getOptionsOrFillCache(executable, project)
        LatexArgumentsCompletionProvider(options).apply(compilerArguments)
        completionCompilerExecutable = executable
    }

    internal fun setValuesForTest(
        compilerPath: String,
        compilerArguments: String,
    ) {
        this.compilerPath.text = compilerPath
        this.compilerArguments.text = compilerArguments
    }

    internal fun currentValuesForTest(): Pair<String, String> = compilerPath.text to compilerArguments.text

    internal fun setCompilerForTest(compiler: LatexCompiler) {
        this.compiler.selectedItem = compiler
        syncOutputFormatOptions(compiler)
        syncCompilerArgumentCompletion(compiler)
    }
}
