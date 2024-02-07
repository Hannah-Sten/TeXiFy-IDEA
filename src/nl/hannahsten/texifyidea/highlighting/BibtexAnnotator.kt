package nl.hannahsten.texifyidea.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.BibtexBracedString
import nl.hannahsten.texifyidea.psi.BibtexBracedVerbatim
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.BibtexKey
import nl.hannahsten.texifyidea.psi.BibtexQuotedString
import nl.hannahsten.texifyidea.psi.BibtexQuotedVerbatim
import nl.hannahsten.texifyidea.util.parser.parentOfType
import nl.hannahsten.texifyidea.util.tokenType
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class BibtexAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is BibtexBracedString -> annotate(element, holder)
            is BibtexBracedVerbatim -> annotate(element, holder)
            is BibtexQuotedString -> annotate(element, holder)
            is BibtexQuotedVerbatim -> annotate(element, holder)
            is BibtexKey -> annotate(element, holder)
        }
    }

    /**
     * Adds syntax highlighting to all {Braced Strings}.
     */
    private fun annotate(bracedString: BibtexBracedString, holder: AnnotationHolder) {
        holder.newAnnotation(HighlightSeverity.INFORMATION, "")
            .range(bracedString)
            .textAttributes(BibtexSyntaxHighlighter.VALUE)
            .create()
    }

    /**
     * Adds syntax highlighting to all {Braced Verbatim Strings}.
     */
    private fun annotate(bracedVerbatim: BibtexBracedVerbatim, holder: AnnotationHolder) {
        holder.newAnnotation(HighlightSeverity.INFORMATION, "")
            .range(bracedVerbatim)
            .textAttributes(BibtexSyntaxHighlighter.VALUE)
            .create()
    }

    /**
     * Adds syntax highlighting to all "Quoted Strings".
     */
    private fun annotate(quotedString: BibtexQuotedString, holder: AnnotationHolder) {
        holder.newAnnotation(HighlightSeverity.INFORMATION, "")
            .range(quotedString)
            .textAttributes(BibtexSyntaxHighlighter.STRING)
            .create()
    }

    /**
     * Adds syntax highlighting to all "Quoted Verbatim Strings".
     */
    private fun annotate(quotedVerbatim: BibtexQuotedVerbatim, holder: AnnotationHolder) {
        holder.newAnnotation(HighlightSeverity.INFORMATION, "")
            .range(quotedVerbatim)
            .textAttributes(BibtexSyntaxHighlighter.STRING)
            .create()
    }

    /**
     * Adds syntax highlighting to all Keys.
     */
    private fun annotate(key: BibtexKey, holder: AnnotationHolder) {
        val entry = key.parentOfType(BibtexEntry::class) ?: return
        val token = entry.tokenType().lowercase(Locale.getDefault())
        if (token == "@string") {
            return
        }

        holder.newAnnotation(HighlightSeverity.INFORMATION, "")
            .range(key)
            .textAttributes(BibtexSyntaxHighlighter.KEY)
            .create()
    }
}