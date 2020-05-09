package nl.hannahsten.texifyidea.run.latex

/**
 * See [LatexDistribution].
 */
enum class LatexDistributionType(val displayName: String) {
    TEXLIVE("TeX Live"),
    MIKTEX("MiKTeX"),
    WSL_TEXLIVE("TeX Live using WSL"),
    DOCKER_MIKTEX("Dockerized MiKTeX");

    override fun toString() = displayName

    companion object {
        fun valueOfIgnoreCase(value: String?): LatexDistributionType {
            return values().firstOrNull { it.name.equals(value, true) } ?: TEXLIVE
        }
    }
}