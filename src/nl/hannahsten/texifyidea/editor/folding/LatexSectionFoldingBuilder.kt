package nl.hannahsten.texifyidea.editor.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.magic.CommandMagic
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

        private val sectionCommandNames = CommandMagic.sectioningCommands.map { it.command }
        private val sectionCommands = sectionCommandNames.map { "\\$it" }.toTypedArray()
    }

    override fun isCollapsedByDefault(node: ASTNode) = false

    override fun getPlaceholderText(node: ASTNode) = node.text + "..."

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        val commands = root.childrenOfType<LatexCommands>().filter { it.name in sectionCommands }
            .sortedBy { it.textOffset }
        val comments = root.childrenOfType<LatexMagicComment>().filter { it.key() == DefaultMagicKeys.FAKE }
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

        for (currentFoldingCommandIndex in sectionElements.indices) {
            var foundHigherCommand = false
            val currentFoldingCommand = sectionElements[currentFoldingCommandIndex]
            val currentCommandRank = sectionCommandNames.indexOf(currentFoldingCommand.name() ?: continue)

            // Find the 'deepest' section under currentFoldingCommand,
            // so when we find something that is equal or lower in rank
            // (a section is ranked lower than a subsection) then we
            // get the block of text between it and the currentFoldingCommand
            for (nextFoldingCommandIndex in currentFoldingCommandIndex + 1 until sectionElements.size) {
                val nextFoldingCommand = sectionElements[nextFoldingCommandIndex]

                // Find the rank of the next command to compare with the current rank
                val nextCommandRank = sectionCommandNames.indexOf(nextFoldingCommand.name())

                // If we found a command which is ranked lower, save the block of text inbetween
                // Note that a section is ranked lower than a subsection
                if (nextCommandRank <= currentCommandRank) {

                    // Get the location of the next folding command
                    val end = nextFoldingCommand.parentOfType(LatexNoMathContent::class)?.previousSiblingIgnoreWhitespace()
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
                val foldingContent = sectionElements.last().parentOfType(LatexNoMathContent::class) ?: continue
                var nextContent: LatexNoMathContent? = foldingContent
                var previousContent: LatexNoMathContent = foldingContent
                while (nextContent != null) {
                    previousContent = nextContent
                    nextContent = nextContent.nextSiblingIgnoreWhitespace() as? LatexNoMathContent
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
