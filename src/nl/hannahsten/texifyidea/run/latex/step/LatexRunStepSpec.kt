package nl.hannahsten.texifyidea.run.latex.step

internal data class LatexRunStepSpec(
    val rawType: String,
) {

    val normalizedType: String = rawType.trim().lowercase()
}
