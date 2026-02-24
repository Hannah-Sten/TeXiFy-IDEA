package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration

internal object LatexRunStepTypeInference {

    fun inferFromRunConfiguration(runConfig: LatexRunConfiguration): List<String> = runConfig.configOptions.steps.map { it.type }
}
