package nl.hannahsten.texifyidea.editor.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import nl.hannahsten.texifyidea.util.childrenOfType

/**
 * Adds folding regions for LaTeX environments.
 *
 * Enables folding of comments.
 *
 * @author jojo2357
 */
class LatexCommentFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun isCollapsedByDefault(node: ASTNode) = false

    override fun getPlaceholderText(node: ASTNode): String {
        val parsedText = node.text.trim()
        return if (parsedText.length > 8) parsedText.substring(0, 8) + "..."
        else parsedText.substring(
            0,
            parsedText.length - 1
        )
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        val comments = root.childrenOfType(PsiComment::class)
        val whitespaces = root.childrenOfType(PsiWhiteSpace::class)

        val whitespaceLocations = ArrayList(whitespaces.map { TextRange(it.startOffset, it.endOffset) })

        // meld friends
        for (i in whitespaceLocations.size - 1 downTo 1) {
            if (whitespaceLocations[i].startOffset == whitespaceLocations[i - 1].endOffset) {
                whitespaceLocations[i - 1] =
                    TextRange(whitespaceLocations[i - 1].startOffset, whitespaceLocations[i].endOffset)
                whitespaceLocations.removeAt(i)
            }
        }

        var collectedTextRange: TextRange? = null
        var parentCollapse: PsiElement? = null

        for (comment in comments) {
            if (collectedTextRange == null) {
                collectedTextRange = TextRange(comment.startOffset, comment.endOffset)
                parentCollapse = comment.originalElement
            }
            else {
                if (whitespaceLocations.any { it.startOffset == collectedTextRange!!.endOffset && it.endOffset == comment.startOffset }) {
                    collectedTextRange = TextRange(collectedTextRange.startOffset, comment.endOffset)
                }
                else {
                    parentCollapse?.let {
                        descriptors.add(
                            FoldingDescriptor(
                                parentCollapse!!,
                                TextRange(collectedTextRange!!.startOffset, collectedTextRange!!.endOffset)
                            )
                        )
                    }
                    collectedTextRange = TextRange(comment.startOffset, comment.endOffset)
                    parentCollapse = comment.originalElement
                }
            }
            if (!whitespaceLocations.any { it.startOffset == collectedTextRange.endOffset }) {
                if (collectedTextRange.endOffset > collectedTextRange.startOffset)
                    parentCollapse?.let {
                        descriptors.add(
                            FoldingDescriptor(
                                parentCollapse,
                                TextRange(collectedTextRange.startOffset, collectedTextRange.endOffset)
                            )
                        )
                    }
            }
        }

        return descriptors.toTypedArray()
    }
}
