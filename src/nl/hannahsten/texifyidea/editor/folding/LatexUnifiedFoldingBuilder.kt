package nl.hannahsten.texifyidea.editor.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.lang.magic.CustomMagicKey
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.psi.key
import nl.hannahsten.texifyidea.util.filterTyped
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.childrenOfType
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import nl.hannahsten.texifyidea.util.parser.nextSiblingIgnoreWhitespace
import nl.hannahsten.texifyidea.util.parser.parentOfType
import nl.hannahsten.texifyidea.util.parser.previousSiblingIgnoreWhitespace

/**
 * Recursively folds section commands
 *
 * @author Tom Evers
 */
open class LatexSectionFoldingBuilder1 : FoldingBuilderEx() {

    private val sectionCommandNames = CommandMagic.sectioningCommands.map { it.command }
    private val sectionCommands = sectionCommandNames.map { "\\$it" }.toTypedArray()

    override fun isCollapsedByDefault(node: ASTNode) = LatexCodeFoldingSettings.getInstance().foldSections

    override fun getPlaceholderText(node: ASTNode) = node.text + "..."

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        // Improvement needed:
        // this should be done in a more efficient way rather than traversing the whole tree
        val children = root.childrenOfType<PsiElement>()
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
                val end = (firstUnmatchedRegion ?: sectionEnd).parentOfType(LatexNoMathContent::class)
                    ?.previousSiblingIgnoreWhitespace()
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
                val endOffset = if (firstUnmatchedRegion != null) firstUnmatchedRegion.startOffset - 1
                else previousContent.textOffset + previousContent.textLength
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

class LatexUnifiedFoldingBuilder : FoldingBuilderEx() {
    /*
    Rules:

    section commands can be ended by a higher level section command or an endregion magic comment.
    region magic comments can only be ended by an endregion magic comment.
     */

    /**
     * A map of section commands (including `\`) to their levels.
     */
    private val sectionLevels: Map<String, Int> = CommandMagic.sectioningCommands.mapIndexed { index, command -> "\\${command.command}" to index }.toMap()

    private val startRegionRegex = """%!\s*(<editor-fold( desc="(?<description>[^"]*)")?>)|(region( (?<description2>\w*))?)""".toRegex()
    private val endRegionRegex = """%!\s*(</editor-fold>|endregion)""".toRegex()


    private fun sectionLevelOf(command: LatexCommands): Int {
        val name = command.name ?: return -1
        return sectionLevels[name] ?: -1
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val visitor = LatexFoldingVisitor()
        root.accept(visitor)
        visitor.endAll(root.textOffset + root.textLength)
        return visitor.descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String = node.text + "..."

    override fun isCollapsedByDefault(node: ASTNode): Boolean =
        LatexCodeFoldingSettings.getInstance().foldSections


    data class SectionCommand(
        val command: PsiElement,
        val level: Int,
        val name: String
    )

    /**
     * We use this visitor to traverse all section commands and magic comments that define regions.
     */
    private inner class LatexFoldingVisitor : LatexCommandOnlyVisitor() {
        /*

        \section

        %! region Section


         */
        val descriptors = mutableListOf<FoldingDescriptor>()

        /**
         * The current section commands found in the document.
         */
        val sectionStack = ArrayDeque<SectionCommand>()

        private fun addSectionCommand(s: SectionCommand, endLevel: Int) {
            if (sectionStack.isEmpty()) {
                // If the stack is empty and the section level is 0, we can add it directly
                sectionStack.addLast(s)
                return
            }
            // If the stack is empty or the current command is at a deeper level than the last one, push it onto the stack
            if (endLevel > sectionStack.last().level) {
                sectionStack.addLast(s)
                return
            }
            endSectionCommand(s.command, endLevel)
            sectionStack.addLast(s) // Add the current command back to the stack
        }

        private fun endSectionCommand(command: PsiElement, level: Int, isEndCommand: Boolean = false) {
            // If the current command is at the same level as the last one, pop the last one and create a folding descriptor
            val prev = command.parentOfType(LatexNoMathContent::class)?.previousSiblingIgnoreWhitespace()
            val endOffset = prev?.endOffset ?: (command.startOffset - 1)
            while (sectionStack.isNotEmpty()) {
                val (lastCommand, lastLevel) = sectionStack.last()
                if (lastLevel < level) {
                    break // The last command is at a lower level, stop popping
                }
                sectionStack.removeLast()
                val foldingRange = TextRange(lastCommand.startOffset, endOffset)
                descriptors.add(FoldingDescriptor(lastCommand, foldingRange))
            }
        }

        private fun endRegionCommand(command: PsiElement) {
            // If the current command is at the same level as the last one, pop the last one and create a folding descriptor
            val endOffset = command.endOffset
            // drop all the
            while (sectionStack.isNotEmpty()) {
                // ignore other section commands and find the last region command, whose level must be Int.MIN_VALUE
                val s = sectionStack.removeLast()
                if (s.level != Int.MIN_VALUE) {
                    continue
                }
                val lastCommand = s.command
                val foldingRange = TextRange(lastCommand.startOffset, endOffset)
                val name = s.name
                descriptors.add(FoldingDescriptor(lastCommand.node, foldingRange, null, name))
                break
            }
        }

        fun endAll(lastOffset: Int) {
            // End all sections that are still open
            while (sectionStack.isNotEmpty()) {
                val (lastCommand, _) = sectionStack.removeLast()
                val foldingRange = TextRange(lastCommand.textOffset, lastOffset)
                descriptors.add(FoldingDescriptor(lastCommand, foldingRange))
            }
        }

        override fun visitCommands(o: LatexCommands) {
            /*
            \section, \subsection, etc. are section commands.
             */
            val element = o
            val name = element.name ?: return
            val level = sectionLevels[name]
            if (level != null && element.firstChild != null &&
                PsiTreeUtil.getParentOfType(element, LatexParameter::class.java) == null
            ) {
                addSectionCommand(SectionCommand(element, level, name), endLevel = level)
            }
            element.acceptChildren(this)
        }

        override fun visitMagicComment(o: LatexMagicComment) {
            val element = o
            val text = element.text
//            if (key != "region" && key != "endregion") {
//                return
//            }
            startRegionRegex.find(text)?.let { match ->
                val groups = match.groups
                var name = groups["description"]?.value ?: groups["description2"]?.value ?: return
                name = name.trim()
                name = "\\$name" // Add the backslash to the name to match the section command names
                val endLevel = sectionLevels[name] ?: Int.MAX_VALUE
                val s = SectionCommand(element, Int.MIN_VALUE, name)
                addSectionCommand(s, endLevel)
                return
            }

            endRegionRegex.find(text)?.let { match ->
                // If the end region is found, we pop the last section command from the stack
                // and create a folding descriptor for it
                endRegionCommand(element)
                return
            }
        }
    }
}