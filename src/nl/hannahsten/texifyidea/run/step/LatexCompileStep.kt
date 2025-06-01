package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.openapi.actionSystem.DataContext
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.ui.LatexCompileSequenceComponent
import javax.swing.JButton

class LatexCompileStep internal constructor(
    override val provider: StepProvider,
    override var configuration: LatexRunConfiguration
) : CompileStep() {

    override val name = "LaTeX compile step"

    override fun configure(context: DataContext, button: LatexCompileSequenceComponent.StepButton) {
    }

    override fun getCommand() = configuration.options.compiler?.getCommand(this) ?: throw TeXception("Skipping step $name because no command is available")

    override fun getWorkingDirectory() = configuration.workingDirectory ?: configuration.options.mainFile.resolve()?.parent?.path

    override fun getEnvironmentVariables() = EnvironmentVariablesData.create(configuration.envs, configuration.isPassParentEnvs)

    override fun clone(): Step {
        return LatexCompileStep(provider, configuration)
    }
}
