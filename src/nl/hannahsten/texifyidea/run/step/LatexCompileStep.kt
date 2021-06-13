package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.configuration.EnvironmentVariablesData
import nl.hannahsten.texifyidea.run.LatexRunConfiguration


class LatexCompileStep(
    override val provider: StepProvider,
    override var configuration: LatexRunConfiguration
) : CompileStep() {

    override fun configure() {

    }

    override fun getCommand() = configuration.options.compiler?.getCommand(this)

    override fun getWorkingDirectory() = configuration.workingDirectory ?: configuration.options.mainFile.resolve()?.parent?.path

    override fun getEnvironmentVariables() = EnvironmentVariablesData.create(configuration.envs, configuration.isPassParentEnvs)
}
