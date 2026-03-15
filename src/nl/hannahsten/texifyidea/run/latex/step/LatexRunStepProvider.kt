package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions

/**
 * Factory contract that maps one step-option type id to a runtime [LatexRunStep].
 * Providers are discovered through [LatexRunStepProviders] during plan building.
 */
internal interface LatexRunStepProvider {

    /**
     * Canonical step id/type used by the provider.
     */
    val type: String

    /**
     * Supported aliases for backward compatibility.
     */
    val aliases: Set<String>

    fun create(stepConfig: LatexStepRunConfigurationOptions): LatexRunStep
}
