package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration


object CompileLatexCompileStepProvider : LatexCompileStepProvider {

    override val name = "Compile LaTeX"

    override val icon = TexifyIcons.BUILD

    override fun createStep(configuration: LatexRunConfiguration) = CompileLatexCompileStep(this, configuration)
}

class CompileLatexCompileStep(
    override val provider: LatexCompileStepProvider, override val configuration: LatexRunConfiguration
) : LatexCompileStep {

    override fun configure() = false

    override fun execute() {
        TODO("Not yet implemented")
    }
}
