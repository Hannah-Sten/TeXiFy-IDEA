package nl.rubensten.texifyidea.util

/**
 * Represents the LaTeX Distribution of the user, e.g. MikTeX or TeX Live.
 */
class LatexDistribution {

    companion object {
        /**
         * Check whether the user is using TeX Live or not.
         */
        fun isTexlive(): Boolean {
            return false
        }

        /**
         * Check whether the user is using MikTeX or not.
         */
        fun isMiktex(): Boolean {
            return false
        }

        /**
         * Find the full name of the distribution in use, e.g. TeX Live 2019.
         */
        fun getDistribution(): String {
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