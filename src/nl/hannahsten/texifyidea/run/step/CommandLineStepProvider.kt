package nl.hannahsten.texifyidea.run.step

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.run.LatexRunConfiguration

object CommandLineStepProvider : StepProvider {

    override val name = "Command line"

    override val icon = TexifyIcons.BUILD // todo

    override val id = "commandline"

    override fun createStep(configuration: LatexRunConfiguration): Step {
        return CommandLineStep(this, configuration)
    }

    override fun createIfRequired(runConfiguration: LatexRunConfiguration): List<Step> {
        // Optional step
        return emptyList()
    }
}