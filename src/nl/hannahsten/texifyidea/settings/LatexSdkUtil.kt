package nl.hannahsten.texifyidea.settings

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.util.runCommand
import java.io.File

object LatexSdkUtil {

    private val pdflatexVersionText: String by lazy {
        getDistribution()
    }

    /**
     * Whether the user is using MikTeX or not.
     * This value is lazy, so only computed when first accessed, because it is unlikely that the user will change LaTeX distribution while using IntelliJ.
     */
    val isMiktexAvailable: Boolean by lazy {
        pdflatexVersionText.contains("MiKTeX")
    }

    /**
     * Whether the user is using TeX Live or not.
     * This value is only computed once.
     */
    val isTexliveAvailable: Boolean by lazy {
        pdflatexVersionText.contains("TeX Live")
    }

    private val isWslTexliveAvailable: Boolean by lazy {
        SystemInfo.isWindows && runCommand("bash", "-ic", "pdflatex --version")?.contains("pdfTeX") == true
    }

    /**
     * Whether the user does not have MiKTeX or TeX Live, but does have the miktex docker image available.
     * In this case we assume the user wants to use Dockerized MiKTeX.
     */
    private fun defaultIsDockerMiktex() =
        (!isMiktexAvailable && !isTexliveAvailable && DockerSdk.isAvailable)

    fun isAvailable(type: LatexDistributionType, project: Project): Boolean {
        if (type == LatexDistributionType.PROJECT_SDK && getLatexProjectSdk(project) != null) return true
        if (type == LatexDistributionType.MIKTEX && isMiktexAvailable) return true
        if (type == LatexDistributionType.TEXLIVE && isTexliveAvailable) return true
        if (type == LatexDistributionType.DOCKER_MIKTEX && DockerSdk.isAvailable) return true
        if (type == LatexDistributionType.WSL_TEXLIVE && isWslTexliveAvailable) return true
        return false
    }

    /**
     * Returns year of texlive installation, 0 if it is not texlive.
     * Assumes the pdflatex version output contains something like (TeX Live 2019).
     */
    val texliveVersion: Int by lazy {
        if (!isTexliveAvailable) {
            0
        }
        else {
            val startIndex = pdflatexVersionText.indexOf("TeX Live")
            try {
                pdflatexVersionText.substring(
                    startIndex + "TeX Live ".length,
                    startIndex + "TeX Live ".length + "2019".length
                ).toInt()
            }
            catch (e: NumberFormatException) {
                0
            }
        }
    }

    /**
     * Given the path to the LaTeX home, find the parent path of the executables, e.g. /bin/x86_64-linux/
     */
    fun getPdflatexParentPath(homePath: String) = File("$homePath/bin").listFiles()?.firstOrNull()?.path

    /**
     * Find the full name of the distribution in use, e.g. TeX Live 2019.
     */
    private fun getDistribution(): String {
        // Could be improved by using the (project-level) LaTeX SDK if pdflatex is not in PATH
        return parsePdflatexOutput(runCommand("pdflatex", "--version") ?: "")
    }

    /**
     * Parse the output of pdflatex --version and return the distribution.
     * Assumes the distribution name is in brackets at the end of the first line.
     */
    fun parsePdflatexOutput(output: String): String {
        val firstLine = output.split("\n")[0]
        val splitLine = firstLine.split("(", ")")

        // Get one-to-last entry, as the last one will be empty after the closing )
        return if (splitLine.size >= 2) {
            splitLine[splitLine.size - 2]
        }
        else {
            ""
        }
    }

    /**
     * Get default LaTeX distribution type (for the run configuration).
     */
    fun getDefaultLatexDistributionType(project: Project): LatexDistributionType {
        return when {
            getLatexProjectSdk(project) != null -> LatexDistributionType.PROJECT_SDK
            isMiktexAvailable -> LatexDistributionType.MIKTEX
            isTexliveAvailable -> LatexDistributionType.TEXLIVE
            defaultIsDockerMiktex() -> LatexDistributionType.DOCKER_MIKTEX
            else -> LatexDistributionType.TEXLIVE
        }
    }

    /**
     * If a LaTeX SDK is selected as project SDK, return it, otherwise return null.
     */
    fun getLatexProjectSdk(project: Project): Sdk? {
        val sdk = ProjectRootManager.getInstance(project).projectSdk
        if (sdk?.sdkType is LatexSdk) {
            return sdk
        }
        return null
    }

    /**
     * Get type of project SDK. If null or not a LaTeX sdk, return null.
     */
    fun getLatexProjectSdkType(project: Project): LatexSdk? {
        return getLatexProjectSdk(project) as? LatexSdk
    }
}