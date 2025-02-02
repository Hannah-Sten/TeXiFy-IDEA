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
        val commands = root.childrenOfType<LatexCommands>().filter { it.name in sectionCommands }
            // If it has no parameters, it is probably not an actual section but in a command definition
            .filter { it.parameterList.isNotEmpty() }
            // Similarly, if the section command is in a parameter it is probably in the preamble, and we should not fold
            .filter { it.firstParentOfType<LatexParameter>() == null }
            .sortedBy { it.textOffset }
        val comments = root.childrenOfType<LatexMagicComment>().filter { it.key() == DefaultMagicKeys.FAKE }
        // If the user has a custom folding region interleaving with sections, e.g. spanning multiple sections but ending inside a section, we give the user priority over the default section folding (so that the user does not need to put fake sections everywhere)
        val customRegions = root.childrenOfType<LatexMagicComment>().filter { it.key() == CustomMagicKey("region") || it.key() == CustomMagicKey("endregion") }
        val sectionElements: List<PsiElement> = (commands + comments).sortedBy { it.textOffset }
        val sectionAndRegionElements: List<PsiElement> = (sectionElements + customRegions).sortedBy { it.textOffset }

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
            var foundHigherCommand = false
            val currentCommandRank = sectionCommandNames.indexOf(currentFoldingCommand.name() ?: continue)

            // Count nested regions
            var customRegionCounter = 0

            // Find the 'deepest' section under currentFoldingCommand,
            // so when we find something that is equal or lower in rank
            // (a section is ranked lower than a subsection) then we
            // get the block of text between it and the currentFoldingCommand
            for (nextFoldingCommand in sectionAndRegionElements.filter { it.startOffset > currentFoldingCommand.startOffset }) {
                // Keep track of custom folding regions within a section that we should skip (we are looking for an endregion that starts before this section, and are assuming that these are balanced)
                // todo now do the same for custom regions that are starting in this section but not ending here
                if (nextFoldingCommand is LatexMagicComment) {
                    if (nextFoldingCommand.key() == CustomMagicKey("region")) {
                        customRegionCounter++
                        continue
                    }
                    else if (nextFoldingCommand.key() == CustomMagicKey("endregion")) {
                        customRegionCounter--
                    }
                    if (customRegionCounter >= 0) continue
                }

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
