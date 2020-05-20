package nl.hannahsten.texifyidea.run.latex

import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.util.runCommand

/**
 * Represents the LaTeX Distribution of the user, e.g. MikTeX or TeX Live.
 */
class LatexDistribution {

    companion object {

        private val pdflatexVersionText: String by lazy {
            getDistribution()
        }

        private val dockerImagesText: String by lazy {
            runCommand("docker", "image", "ls")
        }

        /**
         * Guess the LaTeX distribution that the user probably is using / wants to use.
         */
        val defaultLatexDistribution: LatexDistributionType by lazy {
            when {
                isMiktexAvailable -> LatexDistributionType.MIKTEX
                isTexliveAvailable -> LatexDistributionType.TEXLIVE
                defaultIsDockerMiktex() -> LatexDistributionType.DOCKER_MIKTEX
                else -> LatexDistributionType.TEXLIVE
            }
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

        private val isDockerMiktexAvailable: Boolean by lazy {
            dockerImagesText.contains("miktex")
        }

        private val isWslTexliveAvailable: Boolean by lazy {
            SystemInfo.isWindows && runCommand("bash", "-ic", "pdflatex --version").contains("pdfTeX")
        }

        /**
         * Whether the user does not have MiKTeX or TeX Live, but does have the miktex docker image available.
         * In this case we assume the user wants to use Dockerized MiKTeX.
         */
        private fun defaultIsDockerMiktex() = (!isMiktexAvailable && !isTexliveAvailable && dockerImagesText.contains("miktex"))

        fun isInstalled(type: LatexDistributionType): Boolean {
            if (type == LatexDistributionType.MIKTEX && isMiktexAvailable) return true
            if (type == LatexDistributionType.TEXLIVE && isTexliveAvailable) return true
            if (type == LatexDistributionType.DOCKER_MIKTEX && isDockerMiktexAvailable) return true
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
                    pdflatexVersionText.substring(startIndex + "TeX Live ".length, startIndex + "TeX Live ".length + "2019".length).toInt()
                }
                catch (e: NumberFormatException) {
                    0
                }
            }
        }

        /**
         * Find the full name of the distribution in use, e.g. TeX Live 2019.
         */
        private fun getDistribution(): String {
            return parsePdflatexOutput(runCommand("pdflatex", "--version"))
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
    }
}