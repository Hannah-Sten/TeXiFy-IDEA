package nl.rubensten.texifyidea.util

import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * Represents the LaTeX Distribution of the user, e.g. MikTeX or TeX Live.
 */
class LatexDistribution {

    companion object {
        /**
         * Whether the user is using MikTeX or not.
         * This value is lazy, so only computed when first accessed, because it is unlikely that the user will change LaTeX distribution while using IntelliJ.
         */
        val isMiktex: Boolean by lazy {
            getDistribution().contains("MiKTeX")
        }

        /**
         * Whether the user is using TeX Live or not.
         * This value is only computed once.
         */
        val isTexlive: Boolean by lazy {
            getDistribution().contains("TeX Live")
        }

        /**
         * Find the full name of the distribution in use, e.g. TeX Live 2019.
         */
        private fun getDistribution(): String {
            try {
                val command = arrayListOf("pdflatex", "--version")
                val proc = ProcessBuilder(command)
                        .redirectOutput(ProcessBuilder.Redirect.PIPE)
                        .redirectError(ProcessBuilder.Redirect.PIPE)
                        .start()

                // Timeout value
                proc.waitFor(10, TimeUnit.SECONDS)
                val output = proc.inputStream.bufferedReader().readText()
                return parsePdflatexOutput(output)
            } catch (e: IOException) {
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
            return splitLine[splitLine.size - 2]
        }
    }
}