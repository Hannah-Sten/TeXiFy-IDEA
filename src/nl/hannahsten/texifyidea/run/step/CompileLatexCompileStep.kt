package nl.hannahsten.texifyidea.run.step

import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration


class CompileLatexCompileStep(
    override val provider: LatexCompileStepProvider,
    override val configuration: LatexRunConfiguration
) : LatexCompileStep {

    override fun configure() {

    }

    override fun getCommand() = configuration.compiler?.getCommand(this)
}
