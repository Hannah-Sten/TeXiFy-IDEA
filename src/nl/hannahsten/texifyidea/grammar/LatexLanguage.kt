package nl.hannahsten.texifyidea.grammar

import com.intellij.lang.Language

/**
 * @author Sten Wessel
 */
object LatexLanguage : Language("Latex") {
    private fun readResolve(): Any = LatexLanguage

    override fun getDisplayName(): String {
        return "LaTeX"
    }
}