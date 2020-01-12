package nl.hannahsten.texifyidea.util

import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * Represents the LaTeX Distribution of the user, e.g. MikTeX or TeX Live.
 */
class LatexDistribution {

    companion object {

        val pdflatexVersionText: String by lazy {
            getDistribution()
        }

        /**
         * Whether the user is using MikTeX or not.
         * This value is lazy, so only computed when first accessed, because it is unlikely that the user will change LaTeX distribution while using IntelliJ.
         */
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
                } catch (e: NumberFormatException) {
                    0
                }
            }
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