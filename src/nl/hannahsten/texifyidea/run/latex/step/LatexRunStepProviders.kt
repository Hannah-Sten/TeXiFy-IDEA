package nl.hannahsten.texifyidea.run.latex.step

internal object LatexRunStepProviders {

    val all: List<LatexRunStepProvider> = listOf(
        LatexCompileRunStepProvider,
        PdfViewerRunStepProvider,
    )

    private val byAlias: Map<String, LatexRunStepProvider> = buildMap {
        for (provider in all) {
            for (alias in provider.aliases) {
                put(alias.trim().lowercase(), provider)
            }
        }
    }

    fun find(type: String): LatexRunStepProvider? = byAlias[type.trim().lowercase()]
}
