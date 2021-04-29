package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.ide.macro.MacrosDialog
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.dialog
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.intellij.util.ui.JBDimension
import com.intellij.util.xmlb.annotations.Attribute
import nl.hannahsten.texifyidea.run.compiler.bibtex.BibliographyCompiler
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.ui.compiler.CompilerEditor
import nl.hannahsten.texifyidea.util.magic.CompilerMagic


class BibliographyCompileStep(
    override val provider: CompileStepProvider, override val configuration: LatexRunConfiguration
) : CompileStep, PersistentStateComponent<BibliographyCompileStep.State> {

    // See https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html#implementing-the-state-class
    // Note you can view the result in practice in workspace.xml
    // We are using attributes instead of optiontags for brevity in the xml (it seems to make more sense, as attributes of a compile step)
    // Attributes: <compile-step myattribute="value" />
    // OptionTags (as in LatexRunConfigurationOptions): <option value="optionValue" />
    class State : BaseState() {

        @get:Attribute("compiler", converter = BibliographyCompiler.Converter::class)
        var compiler by property<BibliographyCompiler?>(null) { it == null }

        @get:Attribute
        var compilerArguments by string()

        @get:Attribute
        var workingDirectory by string()

        @get:Attribute
        var envs by map<String, String>()

        @get:Attribute
        var isPassParentEnvs by property(EnvironmentVariablesData.DEFAULT.isPassParentEnvs)
    }

    private var state = State()

    override fun configure() {
        val compilerEditor = CompilerEditor("Compiler", CompilerMagic.bibliographyCompilerByExecutableName.values).apply {
            CommonParameterFragments.setMonospaced(component)
            minimumSize = JBDimension(200, 30)
            label.isVisible = false
            component.setMinimumAndPreferredWidth(150)

            setSelectedCompiler(state.compiler)
        }

        val compilerArguments = ExpandableTextField().apply {
            emptyText.text = "Compiler arguments"
            MacrosDialog.addMacroSupport(this, MacrosDialog.Filters.ALL) { false }
            CommonParameterFragments.setMonospaced(this)

            text = state.compilerArguments
        }

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
            row("Compiler:") {
                cell {
                    component(compilerEditor.component)
                    compilerArguments(CCFlags.growX, CCFlags.pushX)
                }
            }
            row("Working directory:") {
                component(workingDirectory)
                    .comment("Working directory should typically be the directory where the .aux file can be found.")
            }
            row("Environment variables:") {
                component(environmentVariables)
                    .comment("For this step only. Separate variables with semicolon: VAR=value; VAR1=value1")
            }
        }

        val modified = dialog(
            "Configure Bibliography Step",
            panel = panel,
            resizable = true,
            focusedComponent = compilerEditor.component,
        ).showAndGet()

        if (modified) {
            state.compiler = compilerEditor.getSelectedCompiler() as BibliographyCompiler?
            state.compilerArguments = compilerArguments.text.trim().ifEmpty { null }
            state.workingDirectory = workingDirectory.text.trim().ifEmpty { null }
            state.envs = environmentVariables.envData.envs
            state.isPassParentEnvs = environmentVariables.isPassParentEnvs
        }
    }

    override fun getCommand(): List<String>? {
        return state.compiler?.getCommand(this)
    }

    override fun getWorkingDirectory() = state.workingDirectory ?: configuration.getAuxilDirectory()?.path ?: configuration.mainFile?.parent?.path

    override fun getEnvironmentVariables() = EnvironmentVariablesData.create(state.envs, state.isPassParentEnvs)

    override fun getState() = state

    override fun loadState(state: State) {
        state.resetModificationCount()
        this.state = state
    }
}
