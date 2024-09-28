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
import com.intellij.psi.util.startOffset
import com.intellij.psi.util.endOffset
import nl.hannahsten.texifyidea.util.parser.childrenOfType

/**
 * Enables folding of multiple comments on successive lines.
 *
 * @author jojo2357
 */
class LatexCommentFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun isCollapsedByDefault(node: ASTNode) = false

    override fun getPlaceholderText(node: ASTNode): String {
        val parsedText = node.text.trim()
        return if (parsedText.length > 9) parsedText.substring(0, 8) + "..."
        else parsedText
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
            // Initialization: start with the first comment in a possible sequence
            if (collectedTextRange == null) {
                collectedTextRange = TextRange(comment.startOffset, comment.endOffset)
                parentCollapse = comment.originalElement
            }
            else {
                // If the next comment follows directly after the previous one (and was broken by whitespace) add the next comment to the sequence
                if (whitespaceLocations.any { it.startOffset == collectedTextRange!!.endOffset && it.endOffset == comment.startOffset }) {
                    collectedTextRange = TextRange(collectedTextRange.startOffset, comment.endOffset)
                }
                else {
                    // Otherwise, stop the sequence and fold what we have so far
                    parentCollapse?.let {
                        descriptors.add(
                            buildDescriptor(parentCollapse, collectedTextRange)
                        )
                    }
                    collectedTextRange = TextRange(comment.startOffset, comment.endOffset)
                    parentCollapse = comment.originalElement
                }
            }
        }

        // Fold what we were building in the event we run out of comments (aka trailing whitespace in file)
        if (parentCollapse != null && collectedTextRange != null) {
            if (collectedTextRange.endOffset > collectedTextRange.startOffset)
                parentCollapse.let {
                    descriptors.add(buildDescriptor(parentCollapse, collectedTextRange))
                }
            parentCollapse = null
            collectedTextRange = null
        }

        return descriptors.toTypedArray()
    }

    private fun buildDescriptor(
        parentCollapse: PsiElement?,
        collectedTextRange: TextRange?
    ) = FoldingDescriptor(
        parentCollapse!!,
        TextRange(collectedTextRange!!.startOffset, collectedTextRange.endOffset)
    )
}
