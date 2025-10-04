package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.ide.macro.MacrosDialog
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.dialog
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.intellij.util.xmlb.annotations.Attribute
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.compiler.bibtex.BibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.bibtex.CustomBibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.bibtex.SupportedBibliographyCompiler
import nl.hannahsten.texifyidea.run.ui.LatexCompileSequenceComponent
import nl.hannahsten.texifyidea.run.ui.compiler.ExecutableEditor
import nl.hannahsten.texifyidea.util.magic.CompilerMagic
import javax.swing.JButton

class BibliographyCompileStep internal constructor(
    override val provider: StepProvider,
    override var configuration: LatexRunConfiguration
) : CompileStep(), PersistentStateComponent<BibliographyCompileStep.State> {

    override val name = "Bibliography step"

    // See https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html#implementing-the-state-class
    // Note you can view the result in practice in workspace.xml
    // We are using attributes instead of optiontags for brevity in the xml (it seems to make more sense, as attributes of a compile step)
    // Attributes: <compile-step myattribute="value" />
    // OptionTags (as in LatexRunConfigurationOptions): <option value="optionValue" />
    // Note that you cannot use an inner class here, because it will require an instance of the
    // outer class when deserializing, which is at that moment impossible (because we first create
    // a step and a state separately, and then load the state to the step) and will generate a
    // IllegalArgumentException: No argument provided for a required parameter of fun State.<init>()
    // todo it's now slightly too easy to create an instance of this class while forgetting to set one of these state variables
    class State : BaseState() {

        @get:Attribute("compiler", converter = BibliographyCompiler.Converter::class)
        var compiler by property<BibliographyCompiler?>(null) { it == null }

        @get:Attribute
        var compilerArguments by string()

        @get:Attribute
        var workingDirectory by string()

        // todo create UI element for this?
        /** The file name with which to call bibtex, usually the name of the main LaTeX file. */
        @get:Attribute
        var mainFileName by string()

        @get:Attribute
        var envs by map<String, String>()

        @get:Attribute
        var isPassParentEnvs by property(EnvironmentVariablesData.DEFAULT.isPassParentEnvs)
    }

    private var state = State()

    override fun configure(context: DataContext, button: LatexCompileSequenceComponent.StepButton) {
        val executableEditor = ExecutableEditor<SupportedBibliographyCompiler, BibliographyCompiler>("Compiler", CompilerMagic.bibliographyCompilerByExecutableName.values) {
            CustomBibliographyCompiler(it)
        }
        setDefaultLayout(executableEditor, state.compiler)

        val compilerArguments = createParametersTextField("Compiler", state.compilerArguments)

        val workingDirectory = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                "Select Working Directory",
                "Working directory should typically be the directory where the .aux file can be found.",
                configuration.project,
                FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
            )
            // TODO: make these actually work
            MacrosDialog.addMacroSupport(textField as ExtendableTextField, MacrosDialog.Filters.DIRECTORY_PATH) { false }

            text = state.workingDirectory ?: ""
        }

        val environmentVariables = EnvironmentVariablesComponent().apply {
            label.isVisible = false
            envs = state.envs
            isPassParentEnvs = state.isPassParentEnvs
        }

        val panel = panel {
            row(JBLabel("Compiler:")) {
                cell {
                    component(executableEditor.component)
                    compilerArguments(CCFlags.growX, CCFlags.pushX)
                }
            }
            row(JBLabel("Working directory:")) {
                component(workingDirectory)
                    .comment("Working directory should typically be the directory where the .aux file can be found.")
            }
            row(JBLabel("Environment variables:")) {
                component(environmentVariables)
                    .comment("For this step only. Separate variables with semicolon: VAR=value; VAR1=value1")
            }
        }

        val modified = dialog(
            "Configure Bibliography Step",
            panel = panel,
            resizable = true,
            focusedComponent = executableEditor.component,
        ).showAndGet()

        if (modified) {
            state.compiler = executableEditor.getSelectedExecutable()
            state.compilerArguments = compilerArguments.text.trim().ifEmpty { null }
            state.workingDirectory = workingDirectory.text.trim().ifEmpty { null }
            state.envs = environmentVariables.envData.envs
            state.isPassParentEnvs = environmentVariables.isPassParentEnvs
        }
    }

    override fun getCommand(): List<String> {
        // todo support WSL https://github.com/Hannah-Sten/TeXiFy-IDEA/pull/3912/files#diff-80087f53d6ca2679361a05ff35e7c45c137388d8904d262af0bee8ad866e2ea8
        return state.compiler?.getCommand(this) ?: throw TeXception("Unknown bibliography compiler")
    }

    // todo WSL paths https://github.com/Hannah-Sten/TeXiFy-IDEA/pull/3912/files#diff-3f8ede64c3b0656a49b0a714ad747dc48f322de3470b7fc03617aaceaab12671
    override fun getWorkingDirectory() =
        state.workingDirectory ?: configuration.getAuxilDirectory()?.path ?: configuration.options.mainFile.resolve()?.parent?.path

    override fun getEnvironmentVariables() = EnvironmentVariablesData.create(state.envs, state.isPassParentEnvs)

    override fun getState() = state

    override fun loadState(state: State) {
        state.resetModificationCount()
        this.state = state
    }

    override fun clone(): Step {
        // See RunConfigurationBase.clone()
        val newStep = BibliographyCompileStep(provider, configuration)
        newStep.loadState(State())
        newStep.state.copyFrom(this.state)
        return newStep
    }
}
