package nl.hannahsten.texifyidea.run.step

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration

object CompileLatexCompileStepProvider : LatexCompileStepProvider {

    override val name = "Compile LaTeX"

    override val icon = TexifyIcons.BUILD

    override val id = "compile-latex"

    override fun createStep(configuration: LatexRunConfiguration) = CompileLatexCompileStep(this, configuration)
}