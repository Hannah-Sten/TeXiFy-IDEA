package nl.hannahsten.texifyidea.run.step

import nl.hannahsten.texifyidea.run.LatexRunConfiguration


class LatexCompileStep(
    override val provider: CompileStepProvider,
    override val configuration: LatexRunConfiguration
) : CompileStep {

    override fun configure() {

    }

    override fun getCommand() = configuration.compiler?.getCommand(this)

    override fun getWorkingDirectory() = configuration.workingDirectory ?: configuration.mainFile?.parent?.path
}
