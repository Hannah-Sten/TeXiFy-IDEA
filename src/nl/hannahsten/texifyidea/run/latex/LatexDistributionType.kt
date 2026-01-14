package nl.hannahsten.texifyidea.run.latex

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.settings.sdk.LatexSdk
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil

/**
 * Options for the run configuration.
 * See [LatexSdk].
 */
enum class LatexDistributionType(val displayName: String) {

    TEXLIVE("TeX Live"),
    MIKTEX("MiKTeX"),
    WSL_TEXLIVE("TeX Live using WSL"),
    DOCKER_MIKTEX("Dockerized MiKTeX"),
    DOCKER_TEXLIVE("Dockerized TeX Live"),
    PROJECT_SDK("Use project SDK"),
    MODULE_SDK("Use module SDK");

    private fun isMiktex() = this == MIKTEX || this == DOCKER_MIKTEX

    /**
     * Check if this distribution type represents MiKTeX.
     * For SDK-based types, resolves the actual SDK to determine the distribution.
     */
    fun isMiktex(project: Project, mainFile: VirtualFile? = null): Boolean = when (this) {
        MIKTEX, DOCKER_MIKTEX -> true
        PROJECT_SDK -> LatexSdkUtil.getLatexDistributionType(project)?.isMiktex() == true
        MODULE_SDK -> {
            if (mainFile != null) {
                LatexSdkUtil.getLatexDistributionTypeForFile(mainFile, project)?.isMiktex() == true
            }
            else {
                LatexSdkUtil.getLatexDistributionType(project)?.isMiktex() == true
            }
        }
        else -> false
    }

    fun isDocker() = this == DOCKER_MIKTEX || this == DOCKER_TEXLIVE

    fun isAvailable(project: Project) = LatexSdkUtil.isAvailable(this, project)

    override fun toString() = displayName

    companion object {

        fun valueOfIgnoreCase(value: String?): LatexDistributionType = entries.firstOrNull { it.name.equals(value, true) } ?: TEXLIVE
    }
}