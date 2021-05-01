package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.configuration.EnvironmentVariablesData
import nl.hannahsten.texifyidea.run.LatexRunConfiguration


class LatexCompileStep(
    override val provider: CompileStepProvider,
    override val configuration: LatexRunConfiguration
) : CompileStep {

    override fun configure() {

    }

    override fun getCommand() = configuration.compiler?.getCommand(this)

    override fun getWorkingDirectory() = configuration.workingDirectory ?: configuration.mainFile?.parent?.path

    override fun getEnvironmentVariables() = EnvironmentVariablesData.create(configuration.envs, configuration.isPassParentEnvs)
}
