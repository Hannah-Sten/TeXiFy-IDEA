package nl.hannahsten.texifyidea.editor

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.PsiAwareDefaultLineWrapPositionStrategy
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.startOffset
import nl.hannahsten.texifyidea.psi.LatexTypes

/**
 * Based on JavaLineWrapPositionStrategy.
 *
 * @author Thomas
 */
class LatexLineWrapStrategy : PsiAwareDefaultLineWrapPositionStrategy(true, *enabledTypes) {

    companion object {
        // The types that are allowed to be wrapped.
        // (I think only lowest level elements count, unless a parent element contains only one child?)
        // RAW_TEXT_TOKEN is excluded, to automatically exclude urls in \url and \href
        val enabledTypes = arrayOf(
            LatexTypes.COMMENT_TOKEN,
            LatexTypes.REQUIRED_PARAM_CONTENT,
            LatexTypes.NORMAL_TEXT_WORD,
        )
    }

    override fun doCalculateWrapPosition(
        document: Document,
        project: Project?,
        element: PsiElement,
        startOffset: Int,
        endOffset: Int,
        maxPreferredOffset: Int,
        isSoftWrap: Boolean
    ): Int {
        val wrapPosition = super.doCalculateWrapPosition(document, project, element, startOffset, endOffset, maxPreferredOffset, isSoftWrap)

        // Don't wrap urls
        val text = element.text
        @Suppress("HttpUrlsUsage")
        val urlStartInParent = text.indexOfAny(setOf("http://", "https://", "ftp://", "file://", "mailto:"))
        if (urlStartInParent == -1) {
            return wrapPosition
        }

        // Best guess at url end
        val url = text.subSequence(urlStartInParent, text.length).takeWhile { it.isWhitespace().not() }
        val urlEnd = startOffset + urlStartInParent + url.length
        val isInUrl = wrapPosition in element.startOffset + urlStartInParent..urlEnd
        if (!isInUrl || text.substring(urlEnd).isBlank()) return wrapPosition
        // We can still wrap after the url
        // todo same for \href
        return urlEnd
    }
}