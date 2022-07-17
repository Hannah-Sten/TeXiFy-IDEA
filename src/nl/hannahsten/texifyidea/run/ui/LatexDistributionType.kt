package nl.hannahsten.texifyidea.run.ui

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.settings.sdk.LatexSdk
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil

/**
 * Options for the run configuration.
 *
 * See [LatexSdk], which partially supersedes this class.
 * This enum is still required, for example to support non-IntelliJ IDEs, but also to allow overriding the distribution type
 * when for example a different project SDK needs to be selected for a different language to work.
 */
enum class LatexDistributionType(val displayName: String) {

    TEXLIVE("TeX Live"),
    MIKTEX("MiKTeX"),
    WSL_TEXLIVE("TeX Live using WSL"),
    DOCKER_MIKTEX("Dockerized MiKTeX"),
    PROJECT_SDK("Use project SDK");

    private fun isMiktex() = this == MIKTEX || this == DOCKER_MIKTEX
    fun isMiktex(project: Project) = this == MIKTEX || this == DOCKER_MIKTEX || (this == PROJECT_SDK && LatexSdkUtil.getLatexProjectSdkType(project)?.getLatexDistributionType()?.isMiktex() == true)

    fun isAvailable(project: Project) = LatexSdkUtil.isAvailable(this, project)

    override fun toString() = displayName

    companion object {

        fun valueOfIgnoreCase(value: String?): LatexDistributionType {
            return values().firstOrNull { it.name.equals(value, true) } ?: TEXLIVE
        }
    }
}