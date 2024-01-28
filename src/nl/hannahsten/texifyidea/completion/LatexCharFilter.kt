package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.lookup.CharFilter
import com.intellij.codeInsight.lookup.Lookup
import nl.hannahsten.texifyidea.file.LatexFile

/**
 * @author Sten Wessel
 */
class LatexCharFilter : CharFilter() {

    override fun acceptChar(c: Char, prefixLength: Int, lookup: Lookup): Result? {
        return if (!isInLatexContext(lookup)) {
            null
        }
        else when (c) {
            '$' -> Result.HIDE_LOOKUP
            ':' -> Result.ADD_TO_PREFIX
            else -> null
        }
    }

    private fun isInLatexContext(lookup: Lookup): Boolean {
        if (!lookup.isCompletion) {
            return false
        }
        val element = lookup.psiElement
        val file = lookup.psiFile
        return file is LatexFile && element != null
    }
}