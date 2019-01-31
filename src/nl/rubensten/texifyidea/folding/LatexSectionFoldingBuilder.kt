package nl.rubensten.texifyidea.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.psi.LatexContent
import nl.rubensten.texifyidea.util.nextSiblingIgnoreWhitespace
import nl.rubensten.texifyidea.util.parentOfType
import nl.rubensten.texifyidea.util.previousSiblingIgnoreWhitespace

/**
 * Recursively folds section commands
 *
 * @author Tom Evers
 */
open class LatexSectionFoldingBuilder : FoldingBuilderEx() {

    companion object {
        private val sectionCommands = arrayOf(
                "\\part", "\\chapter",
                "\\section", "\\subsection", "\\subsubsection",
                "\\paragraph", "\\subparagraph"
            )
    }

    override fun isCollapsedByDefault(node: ASTNode) = false

    override fun getPlaceholderText(node: ASTNode) = node.text + "..."

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        val commands = LatexCommandsIndex.getCommandsByNames(root.containingFile, *sectionCommands).toList()
                .sortedBy { it.textOffset }
        if (commands.isEmpty()) {
            return descriptors.toTypedArray()
        }

        for (currentFoldingCommandIndex in 0 until commands.size) {
            var foundHigherCommand = false
            val currentFoldingCommand = commands[currentFoldingCommandIndex]
            val currentCommandRank = sectionCommands.indexOf(currentFoldingCommand.name)
            for (nextFoldingCommandIndex in currentFoldingCommandIndex + 1 until commands.size) {
                val nextFoldingCommand = commands[nextFoldingCommandIndex]
                val nextCommandRank = sectionCommands.indexOf(nextFoldingCommand.name)
                if (currentCommandRank >= nextCommandRank) {
                    val end = nextFoldingCommand.parentOfType(LatexContent::class)?.previousSiblingIgnoreWhitespace()
                            ?: break
                    val foldingRange = TextRange(currentFoldingCommand.textOffset, end.textOffset + end.textLength)
                    if (foldingRange.length > 0) {
                        descriptors.add(FoldingDescriptor(currentFoldingCommand, foldingRange))
                    }
                    foundHigherCommand = true
                    break
                }
            }
            /* If this item is the topmost level of the section structure or the last section marker,
             * use the end of all text as the end of the range.
             */
            if (!foundHigherCommand) {
                val foldingContent = commands.last().parentOfType(LatexContent::class) ?: continue // TODO: Check this
                var nextContent: LatexContent? = foldingContent
                var previousContent: LatexContent = foldingContent
                while (nextContent != null) {
                    previousContent = nextContent
                    nextContent = nextContent.nextSiblingIgnoreWhitespace() as? LatexContent
                }
                val foldingRange = TextRange(
                        currentFoldingCommand.textOffset,
                        previousContent.textOffset + previousContent.textLength
                )
                if (foldingRange.length > 0) {
                    descriptors.add(FoldingDescriptor(currentFoldingCommand, foldingRange))
                }
            }
        }
        return descriptors.toTypedArray()
    }
}
