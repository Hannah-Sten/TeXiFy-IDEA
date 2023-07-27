package nl.hannahsten.texifyidea.grammar

import com.intellij.lang.Language

/**
 * @author Hannah Schellekens
 */
object BibtexLanguage : Language("Bibtex") {
    private fun readResolve(): Any = BibtexLanguage

    override fun getDisplayName(): String = "BibTeX"
}