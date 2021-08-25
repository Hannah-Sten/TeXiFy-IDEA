package nl.hannahsten.texifyidea.run.step

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.run.LatexRunConfiguration

object LatexCompileStepProvider : StepProvider {

    override val name = "Compile LaTeX"

    override val icon = TexifyIcons.BUILD

    override val id = "compile-latex"

    override fun createStep(configuration: LatexRunConfiguration) = LatexCompileStep(this, configuration)

    override fun createIfRequired(runConfiguration: LatexRunConfiguration): List<Step> {
        // LaTeX step is always required
        // todo whether we need to add even more steps, depends on requirements of other steps?
        return listOf(createStep(runConfiguration))
    }

}