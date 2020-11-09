package nl.hannahsten.texifyidea.run.latex

import nl.hannahsten.texifyidea.settings.LatexSdk

/**
 * See [LatexSdk].
 */
enum class LatexDistributionType(val displayName: String) {
    TEXLIVE("TeX Live"),
    MIKTEX("MiKTeX"),
    WSL_TEXLIVE("TeX Live using WSL"),
    DOCKER_MIKTEX("Dockerized MiKTeX");

    fun isMiktex() = this == MIKTEX || this == DOCKER_MIKTEX

    fun isTexlive() = this == TEXLIVE || this == WSL_TEXLIVE

    fun isInstalled() = LatexSdk.isInstalled(this)

    override fun toString() = displayName

    companion object {
        fun valueOfIgnoreCase(value: String?): LatexDistributionType {
            return values().firstOrNull { it.name.equals(value, true) } ?: LatexSdk.defaultLatexDistribution
        }
    }
}