package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexStepConfig

internal data class LatexRunStepPlan(
    val steps: List<LatexRunStep>,
    val unsupportedTypes: List<String>,
)

internal object LatexRunStepPlanBuilder {

    fun build(stepConfigs: List<LatexStepConfig>): LatexRunStepPlan {
        val steps = mutableListOf<LatexRunStep>()
        val unsupported = mutableListOf<String>()

        for (stepConfig in stepConfigs) {
            val provider = LatexRunStepProviders.find(stepConfig.type)
            if (provider == null) {
                unsupported += stepConfig.type
            }
            else {
                steps += provider.create(stepConfig)
            }
        }

        return LatexRunStepPlan(steps = steps, unsupportedTypes = unsupported)
    }
}
