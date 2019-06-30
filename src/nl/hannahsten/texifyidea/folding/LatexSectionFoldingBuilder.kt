package nl.hannahsten.texifyidea.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.psi.LatexContent
import nl.hannahsten.texifyidea.util.nextSiblingIgnoreWhitespace
import nl.hannahsten.texifyidea.util.parentOfType
import nl.hannahsten.texifyidea.util.previousSiblingIgnoreWhitespace

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

            // Find the 'deepest' section under currentFoldingCommand,
            // so when we find something that is equal or lower in rank
            // (a section is ranked lower than a subsection) then we
            // get the block of text between it and the currentFoldingCommand
            for (nextFoldingCommandIndex in currentFoldingCommandIndex + 1 until commands.size) {
                val nextFoldingCommand = commands[nextFoldingCommandIndex]

                // Find the rank of the next command to compare with the current rank
                val nextCommandRank = sectionCommands.indexOf(nextFoldingCommand.name)

                // If we found a command which is ranked lower, save the block of text inbetween
                // Note that a section is ranked lower than a subsection
                if (nextCommandRank <= currentCommandRank) {

                    // Get the location of the next folding command
                    val end = nextFoldingCommand.parentOfType(LatexContent::class)?.previousSiblingIgnoreWhitespace()
                            ?: break

                    // Get the text range between the current and the next folding command
                    if (end.textOffset + end.textLength - currentFoldingCommand.textOffset > 0) {

                        val foldingRange = TextRange(currentFoldingCommand.textOffset, end.textOffset + end.textLength)

                        // Add it as a folding block
                        descriptors.add(FoldingDescriptor(currentFoldingCommand, foldingRange))
                    }

                    foundHigherCommand = true
                    break
                }
            }

            /*
             * If this item is the topmost level of the section structure or the last section marker,
             * use the end of all text as the end of the range.
             */
            if (!foundHigherCommand) {
                val foldingContent = commands.last().parentOfType(LatexContent::class) ?: continue
                var nextContent: LatexContent? = foldingContent
                var previousContent: LatexContent = foldingContent
                while (nextContent != null) {
                    previousContent = nextContent
                    nextContent = nextContent.nextSiblingIgnoreWhitespace() as? LatexContent
                }
                if (previousContent.textOffset + previousContent.textLength - currentFoldingCommand.textOffset > 0) {
                    val foldingRange = TextRange(
                            currentFoldingCommand.textOffset,
                            previousContent.textOffset + previousContent.textLength
                    )
                    descriptors.add(FoldingDescriptor(currentFoldingCommand, foldingRange))
                }
            }
        }
        return descriptors.toTypedArray()
    }
}
