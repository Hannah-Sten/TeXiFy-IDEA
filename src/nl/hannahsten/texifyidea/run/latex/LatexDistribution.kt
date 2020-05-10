package nl.hannahsten.texifyidea.run.latex

import nl.hannahsten.texifyidea.settings.TexifySettings
import java.io.IOException
import java.util.concurrent.TimeUnit


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
                isMiktex -> LatexDistributionType.MIKTEX
                isTexlive -> LatexDistributionType.TEXLIVE
                isDockerMiktex() -> LatexDistributionType.DOCKER_MIKTEX
                else -> LatexDistributionType.TEXLIVE
            }
        }

        /**
         * Whether the user is using MikTeX or not.
         * This value is lazy, so only computed when first accessed, because it is unlikely that the user will change LaTeX distribution while using IntelliJ.
         */
        // todo check usage
        val isMiktex: Boolean by lazy {
            pdflatexVersionText.contains("MiKTeX")
        }

        /**
         * Whether the user is using TeX Live or not.
         * This value is only computed once.
         */
        val isTexlive: Boolean by lazy {
            pdflatexVersionText.contains("TeX Live")
        }

        /**
         * Whether the user does not have MiKTeX or TeX Live, but does have the miktex docker image available.
         */
        fun isDockerMiktex() = TexifySettings.getInstance().dockerizedMiktex || (!isMiktex && !isTexlive && dockerImagesText.contains("miktex"))

        /**
         * Returns year of texlive installation, 0 if it is not texlive.
         * Assumes the pdflatex version output contains something like (TeX Live 2019).
         */
        val texliveVersion: Int by lazy {
            if (!isTexlive) {
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

        private fun runCommand(vararg commands: String): String {
            try {
                val command = arrayListOf(*commands)
                val proc = ProcessBuilder(command)
                        .redirectOutput(ProcessBuilder.Redirect.PIPE)
                        .redirectError(ProcessBuilder.Redirect.PIPE)
                        .start()

                // Timeout value
                proc.waitFor(10, TimeUnit.SECONDS)
                return proc.inputStream.bufferedReader().readText()
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
            return ""
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