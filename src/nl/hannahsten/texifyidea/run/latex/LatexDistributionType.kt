package nl.hannahsten.texifyidea.run.latex

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.settings.sdk.LatexSdk
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil

/**
 * Options for the run configuration.
 * See [LatexSdk].
 */
enum class LatexDistributionType(private val messageKey: String) {

    TEXLIVE("run.latex.distribution.texlive"),
    MIKTEX("run.latex.distribution.miktex"),
    WSL_TEXLIVE("run.latex.distribution.wsl.texlive"),
    DOCKER_MIKTEX("run.latex.distribution.docker.miktex"),
    DOCKER_TEXLIVE("run.latex.distribution.docker.texlive"),
    PROJECT_SDK("run.latex.distribution.project.sdk"),
    MODULE_SDK("run.latex.distribution.module.sdk");

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

    override fun toString() = TexifyBundle.message(messageKey)

    companion object {

        fun valueOfIgnoreCase(value: String?): LatexDistributionType = entries.firstOrNull { it.name.equals(value, true) } ?: TEXLIVE
    }
}
