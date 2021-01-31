package nl.hannahsten.texifyidea

import com.intellij.lang.Language

/**
 * @author Sten Wessel
 */
class LatexLanguage private constructor() : Language("Latex") {

    override fun getDisplayName(): String {
        return "LaTeX"
    }

    companion object {

        @JvmField
        val INSTANCE = LatexLanguage()
    }
}