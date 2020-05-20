package nl.hannahsten.texifyidea.run.latex

/**
 * See [LatexDistribution].
 */
enum class LatexDistributionType(val displayName: String) {
    TEXLIVE("TeX Live"),
    MIKTEX("MiKTeX"),
    WSL_TEXLIVE("TeX Live using WSL"),
    DOCKER_MIKTEX("Dockerized MiKTeX");

    fun isMiktex() = this == MIKTEX || this == DOCKER_MIKTEX

    fun isTexlive() = this == TEXLIVE || this == WSL_TEXLIVE

    fun isInstalled() = LatexDistribution.isInstalled(this)

    override fun toString() = displayName

    companion object {
        fun valueOfIgnoreCase(value: String?): LatexDistributionType {
            return values().firstOrNull { it.name.equals(value, true) } ?: LatexDistribution.defaultLatexDistribution
        }
    }
}