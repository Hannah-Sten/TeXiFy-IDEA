package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.lookup.CharFilter
import com.intellij.codeInsight.lookup.Lookup
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

/**
 * @author Sten Wessel
 */
class LatexCharFilter : CharFilter() {

    override fun acceptChar(c: Char, prefixLength: Int, lookup: Lookup): Result? {
        if (!isInLatexContext(lookup)) return null

        return when (c) {
            '$' -> Result.HIDE_LOOKUP
            ':' -> Result.ADD_TO_PREFIX
            // Allow space in cite commands to search with multiple terms
            ' ' -> if (isInCiteContext(lookup)) Result.ADD_TO_PREFIX else null
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

    private fun isInCiteContext(lookup: Lookup): Boolean {
        val element = lookup.psiElement ?: return false
        val command = element.firstParentOfType(LatexCommands::class) ?: return false
        return command.name in CommandMagic.bibliographyReference
    }
}