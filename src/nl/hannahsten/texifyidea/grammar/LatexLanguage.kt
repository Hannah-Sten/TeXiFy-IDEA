package nl.hannahsten.texifyidea.grammar

import com.intellij.lang.Language
import nl.hannahsten.texifyidea.TexifyBundle

/**
 * @author Sten Wessel
 */
object LatexLanguage : Language("Latex") {
    private fun readResolve(): Any = LatexLanguage

    override fun getDisplayName(): String = TexifyBundle.message("language.latex.displayName")
}
