package nl.hannahsten.texifyidea.editor.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.lang.magic.CustomMagicKey
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.filterTyped
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.*

/**
 * Recursively folds section commands
 *
 * @author Tom Evers
 */
open class LatexSectionFoldingBuilder : FoldingBuilderEx() {

    private val sectionCommandNames = CommandMagic.sectioningCommands.map { it.command }
    private val sectionCommands = sectionCommandNames.map { "\\$it" }.toTypedArray()

    override fun isCollapsedByDefault(node: ASTNode) = LatexCodeFoldingSettings.getInstance().foldSections

    override fun getPlaceholderText(node: ASTNode) = node.text + "..."

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        val children = root.children
        val commands = children.filterTyped<LatexCommands> {
            // If it has no parameters, it is probably not an actual section but in a command definition
            it.name in sectionCommands
                && it.parameterList.isNotEmpty()
                // Similarly, if the section command is in a parameter it is probably in the preamble, and we should not fold
                && it.firstParentOfType<LatexParameter>() == null
        }.sortedBy { it.textOffset }
        val comments = children.filterTyped<LatexMagicComment> {
            it.key() == DefaultMagicKeys.FAKE
        }
        // If the user has a custom folding region interleaving with sections, e.g. spanning multiple sections but ending inside a section, we give the user priority over the default section folding (so that the user does not need to put fake sections everywhere)
        val customRegions = children.filterTyped<LatexMagicComment> {
            it.isStartRegion() || it.isEndRegion()
        }
        val sectionElements: List<PsiElement> = (commands + comments).sortedBy { it.textOffset }

        if (sectionElements.isEmpty()) {
            return descriptors.toTypedArray()
        }

        /**
         * Get the name/section value from a [LatexCommands] or a [LatexMagicComment].
         */
        fun PsiElement.name() = when (this) {
            is LatexCommands -> name?.dropWhile { it == '\\' }
            // Take the first word of the value because the fake section can have a 'Title',
            // e.g. %! fake subsection Introduction.
            is LatexMagicComment -> value()?.trim()?.split(" ")?.firstOrNull()
            else -> null
        }

        for (currentFoldingCommand in sectionElements) {
            val currentCommandRank = sectionCommandNames.indexOf(currentFoldingCommand.name() ?: continue)

            // Find the 'deepest' section under currentFoldingCommand,
            // so when we find something that is equal or lower in rank
            // (a section is ranked lower than a subsection) then we
            // get the block of text between it and the currentFoldingCommand
            val nextElements = sectionElements.filter { it.startOffset > currentFoldingCommand.startOffset }
            // If we found a command which is ranked lower, save the block of text inbetween
            // Note that a section is ranked lower than a subsection
            val sectionEnd = nextElements.firstOrNull { sectionCommandNames.indexOf(it.name()) <= currentCommandRank }

            // Keep track of custom folding regions within a section that we should skip
            // We are looking for an endregion that starts before this section, or a start region that ends after this section,
            // so effectively we are looking for the first unmatched (within this section) region command
            val firstUnmatchedRegion = customRegions
                .filter { it.startOffset > currentFoldingCommand.startOffset }
                .filter { if (sectionEnd == null) true else it.startOffset < sectionEnd.startOffset }
                .fold(listOf<LatexMagicComment>()) { unmatchedIndices, comment ->
                    if (comment.isStartRegion()) {
                        unmatchedIndices + comment
                    }
                    // If the last one is an end, it is already not matched - don't remove it
                    else if (unmatchedIndices.lastOrNull()?.isStartRegion() == true) {
                        unmatchedIndices.dropLast(1)
                    }
                    else {
                        unmatchedIndices + comment
                    }
                }
                .firstOrNull()

            if (sectionEnd != null) {
                // Get the location of the next folding command
                val end = (firstUnmatchedRegion ?: sectionEnd).parentOfType(LatexNoMathContent::class)?.previousSiblingIgnoreWhitespace()
                    ?: break

                // Get the text range between the current and the next folding command
                if (end.textOffset + end.textLength - currentFoldingCommand.textOffset > 0) {
                    val foldingRange = TextRange(currentFoldingCommand.textOffset, end.textOffset + end.textLength)

                    // Add it as a folding block
                    descriptors.add(FoldingDescriptor(currentFoldingCommand, foldingRange))
                }
            }
            // If this item is the topmost level of the section structure or the last section marker,
            // use the end of all text as the end of the range.
            else {
                val foldingContent = sectionElements.last().parentOfType(LatexNoMathContent::class) ?: continue
                var nextContent: LatexNoMathContent? = foldingContent
                var previousContent: LatexNoMathContent = foldingContent
                while (nextContent != null) {
                    previousContent = nextContent
                    nextContent = nextContent.nextSiblingIgnoreWhitespace() as? LatexNoMathContent
                }
                val endOffset = if (firstUnmatchedRegion != null) firstUnmatchedRegion.startOffset - 1 else previousContent.textOffset + previousContent.textLength
                if (endOffset - currentFoldingCommand.textOffset > 0) {
                    val foldingRange = TextRange(
                        currentFoldingCommand.textOffset,
                        endOffset
                    )
                    descriptors.add(FoldingDescriptor(currentFoldingCommand, foldingRange))
                }
            }
        }
        return descriptors.toTypedArray()
    }

    private fun LatexMagicComment.isStartRegion(): Boolean = key() == CustomMagicKey("region")
    private fun LatexMagicComment.isEndRegion(): Boolean = key() == CustomMagicKey("endregion")
}
