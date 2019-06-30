package nl.hannahsten.texifyidea.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.BibtexBracedString
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.BibtexKey
import nl.hannahsten.texifyidea.psi.BibtexQuotedString
import nl.hannahsten.texifyidea.util.parentOfType
import nl.hannahsten.texifyidea.util.tokenType

/**
 * @author Hannah Schellekens
 */
open class BibtexAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is BibtexBracedString -> annotate(element, holder)
            is BibtexQuotedString -> annotate(element, holder)
            is BibtexKey -> annotate(element, holder)
        }
    }

    /**
     * Adds syntax highlighting to all {Braced Strings}.
     */
    private fun annotate(bracedString: BibtexBracedString, holder: AnnotationHolder) {
        val annotation = holder.createInfoAnnotation(bracedString, null)
        annotation.textAttributes = BibtexSyntaxHighlighter.VALUE
    }

    /**
     * Adds syntax highlighting to all "Quoted Strings".
     */
    private fun annotate(quotedString: BibtexQuotedString, holder: AnnotationHolder) {
        val annotation = holder.createInfoAnnotation(quotedString, null)
        annotation.textAttributes = BibtexSyntaxHighlighter.STRING
    }

    /**
     * Adds syntax highlighting to all Keys.
     */
    private fun annotate(key: BibtexKey, holder: AnnotationHolder) {
        val entry = key.parentOfType(BibtexEntry::class) ?: return
        val token = entry.tokenType()?.toLowerCase()
        if (token == "@string") {
            return
        }

        val annotation = holder.createInfoAnnotation(key, null)
        annotation.textAttributes = BibtexSyntaxHighlighter.KEY
    }
}