package nl.hannahsten.texifyidea.grammar

import com.intellij.lang.Language
import nl.hannahsten.texifyidea.TexifyBundle

/**
 * @author Hannah Schellekens
 */
object BibtexLanguage : Language("Bibtex") {
    private fun readResolve(): Any = BibtexLanguage

    override fun getDisplayName(): String = TexifyBundle.message("language.bibtex.displayName")
}
