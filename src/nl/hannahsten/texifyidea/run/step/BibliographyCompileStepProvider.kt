package nl.hannahsten.texifyidea.run.step

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.run.LatexRunConfiguration

object BibliographyCompileStepProvider : StepProvider {

    override val name = "Bibliography"

    override val icon = TexifyIcons.BUILD_BIB

    override val id = "bibliography"

    override fun createStep(configuration: LatexRunConfiguration) = BibliographyCompileStep(this, configuration)
}