package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.dialog
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.intellij.util.xmlb.annotations.Attribute
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import java.awt.event.MouseEvent

/**
 * todo clean up this mess and duplicate code
 *
 * todo can we replace this by the built-in 'run external tool'? See ToolBeforeRunTask
 */
class CommandLineStep internal constructor(
    override val provider: StepProvider,
    override var configuration: LatexRunConfiguration
) : CompileStep(), PersistentStateComponent<CommandLineStep.State> {
    private var state = State()

    override val name = "Command Line step"

    class State : BaseState() {
        @get:Attribute
        var commandLine by string()

        @get:Attribute
        var workingDirectory by string()

        @get:Attribute
        var envs by map<String, String>()

        @get:Attribute
        var isPassParentEnvs by property(EnvironmentVariablesData.DEFAULT.isPassParentEnvs)
    }

    override fun getCommand(): List<String>? {
        return this.state.commandLine?.split(" ") // todo BAD
    }

    override fun getWorkingDirectory(): String? {
        return state.workingDirectory
    }

    override fun getEnvironmentVariables() = EnvironmentVariablesData.create(state.envs, state.isPassParentEnvs)

    override fun configure(context: DataContext) {
        // todo duplicate from bib step

        val commandLineField = createParametersTextField("Command line", state.commandLine)

        val workingDirectory = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                "Select Working Directory",
                "Working directory should typically be the directory where the .aux file can be found.",
                configuration.project,
                FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
            )

            text = state.workingDirectory ?: ""
        }

        val environmentVariables = EnvironmentVariablesComponent().apply {
            label.isVisible = false
            envs = state.envs
            isPassParentEnvs = state.isPassParentEnvs
        }

        val panel = panel {
            row("Command line:") {
                commandLineField(CCFlags.growX, CCFlags.pushX).comment("Any command that should be executed.")
            }
            row("Working directory:") {
                component(workingDirectory)
            }
            row("Environment variables:") {
                component(environmentVariables)
                    .comment("For this step only. Separate variables with semicolon: VAR=value; VAR1=value1")
            }
        }

        val modified = dialog(
            "Configure Command Line Step",
            panel = panel,
            resizable = true,
        ).showAndGet()

        if (modified) {
            state.commandLine = commandLineField.text.trim().ifEmpty { null }
            state.workingDirectory = workingDirectory.text.trim().ifEmpty { null }
            state.envs = environmentVariables.envData.envs
            state.isPassParentEnvs = environmentVariables.isPassParentEnvs
        }

    }

    override fun getState() = state

    override fun loadState(state: State) {
        state.resetModificationCount()
        this.state = state
    }

    override fun clone(): Step {
        // See RunConfigurationBase.clone()
        val newStep = CommandLineStep(provider, configuration)
        newStep.loadState(State())
        newStep.state.copyFrom(this.state)
        return newStep
    }
}