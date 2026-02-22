package nl.hannahsten.texifyidea.run.latex.step

internal interface LatexRunStepProvider {

    /**
     * Canonical step id/type used by the provider.
     */
    val type: String

    /**
     * Supported aliases for backward compatibility.
     */
    val aliases: Set<String>

    fun create(spec: LatexRunStepSpec): LatexRunStep
}
