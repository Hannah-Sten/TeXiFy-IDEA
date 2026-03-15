package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions

/**
 * Execution-ready representation of configured steps.
 * It contains resolved runtime steps and step types that had no matching provider.
 */
internal data class LatexRunStepPlan(
    val steps: List<LatexRunStep>,
    val unsupportedTypes: List<String>,
)

/**
 * Builds [LatexRunStepPlan] from persisted step options.
 * This layer resolves providers and does not execute any step itself.
 */
internal object LatexRunStepPlanBuilder {

    fun build(stepConfigs: List<LatexStepRunConfigurationOptions>): LatexRunStepPlan {
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
