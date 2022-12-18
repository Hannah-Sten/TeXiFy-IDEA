package nl.hannahsten.texifyidea.grammar

import com.intellij.lang.Language

/**
 * @author Sten Wessel
 */
object LatexLanguage : Language("Latex") {

    override fun getDisplayName(): String {
        return "LaTeX"
    }
}