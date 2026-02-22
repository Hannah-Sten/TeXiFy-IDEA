package nl.hannahsten.texifyidea.run.latex.step

internal data class LatexRunStepPlan(
    val steps: List<LatexRunStep>,
    val unsupportedTypes: List<String>,
)

internal object LatexRunStepPlanBuilder {

    fun build(stepTypes: List<String>): LatexRunStepPlan {
        val steps = mutableListOf<LatexRunStep>()
        val unsupported = mutableListOf<String>()

        for (raw in stepTypes) {
            val spec = LatexRunStepSpec(raw)
            val provider = LatexRunStepProviders.find(spec.normalizedType)
            if (provider == null) {
                unsupported += raw
            }
            else {
                steps += provider.create(spec)
            }
        }

        return LatexRunStepPlan(steps = steps, unsupportedTypes = unsupported)
    }
}
