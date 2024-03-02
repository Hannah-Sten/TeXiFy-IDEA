package nl.hannahsten.texifyidea.editor

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.PsiAwareDefaultLineWrapPositionStrategy
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexMathEnvironment
import nl.hannahsten.texifyidea.psi.LatexRawText
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

/**
 * Based on JavaLineWrapPositionStrategy.
 *
 * @author Thomas
 */
class LatexLineWrapStrategy : PsiAwareDefaultLineWrapPositionStrategy(true, *Util.enabledTypes) {

    object Util {
        // The types that are allowed to be wrapped.
        // (I think only lowest level elements count, unless a parent element contains only one child?)
        val enabledTypes = arrayOf(
            LatexTypes.COMMENT_TOKEN,
            LatexTypes.REQUIRED_PARAM_CONTENT,
            LatexTypes.NORMAL_TEXT_WORD,
            LatexTypes.RAW_TEXT_TOKEN,
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
        // math.startOffset may be smaller than startOffset, but I'm not sure why we would not be allowed to return a value < startOffset because that's the only sensible place to wrap: the start of the inline math environment
        val math = element.firstParentOfType<LatexMathEnvironment>()
        if (math != null) return math.startOffset

        // What we're doing here looks very much like LatexLineWrapper#calculatePreferredWrapPosition, should we use that as default instead?
        val wrapPosition = super.doCalculateWrapPosition(document, project, element, startOffset, endOffset, maxPreferredOffset, isSoftWrap)

        // Don't wrap urls, both in comments and in \url-like commands
        if (element is PsiComment) {
            return getWrapPositionInComment(element, wrapPosition)
        }
        if (element.node.elementType == LatexTypes.RAW_TEXT_TOKEN) {
            return getWrapPositionInRawText(element, wrapPosition)
        }

        // Not sure if we want to wrap inside commands
        val command = element.firstParentOfType<LatexCommands>()
        if (command != null) return command.startOffset

        // Don't wrap in the middle of a word (can happen by default for German characters)
        if (element.node.elementType == LatexTypes.NORMAL_TEXT_WORD) {
            return element.startOffset
        }

        return wrapPosition
    }

    private fun String.findUrl(): Int {
        @Suppress("HttpUrlsUsage")
        return this.indexOfAny(setOf("http://", "https://", "ftp://", "file://", "mailto:"))
    }

    /**
     * Don't wrap urls in comments.
     */
    private fun getWrapPositionInComment(
        element: PsiElement,
        defaultWrapPosition: Int
    ): Int {
        val text = element.text
        val urlStartInText = text.findUrl()
        if (urlStartInText == -1) return defaultWrapPosition

        // Best guess at url end in comments
        val url = text.subSequence(urlStartInText, text.length).takeWhile { it.isWhitespace().not() }
        val urlEnd = element.startOffset + urlStartInText + url.length
        val isInUrl = defaultWrapPosition in element.startOffset + urlStartInText..urlEnd
        if (!isInUrl) return defaultWrapPosition

        // Don't wrap if there is nothing to wrap
        if (text.substring(urlStartInText + url.length).isBlank()) return NO_ELEMENT_WRAP

        // We can still wrap after the url if there is non-blank normal text
        return urlEnd
    }

    /**
     * Don't wrap urls in \url-like commands.
     */
    private fun getWrapPositionInRawText(
        element: PsiElement,
        defaultWrapPosition: Int
    ): Int {
        // In case of the argument of \href and \url, we are given a leaf element (not the RAW_TEXT element) so we need to find the whole url
        val elementWithUrl = element.firstParentOfType(LatexRawText::class) ?: return defaultWrapPosition
        val urlIndex = elementWithUrl.text.findUrl()
        if (urlIndex == -1 && defaultWrapPosition != -1) return defaultWrapPosition

        // We don't wrap in the middle of the command, but we could also wrap in the text description of \href
        val urlEnd = elementWithUrl.firstParentOfType(LatexCommands::class)?.endOffset
        return urlEnd ?: defaultWrapPosition
    }
}