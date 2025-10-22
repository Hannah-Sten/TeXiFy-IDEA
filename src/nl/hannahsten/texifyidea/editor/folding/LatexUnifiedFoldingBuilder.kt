package nl.hannahsten.texifyidea.editor.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.descendants
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.lang.predefined.EnvironmentNames
import nl.hannahsten.texifyidea.lang.predefined.PredefinedCmdPairedDelimiters
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.endOffset
import nl.hannahsten.texifyidea.psi.prevContextualSiblingIgnoreWhitespace
import nl.hannahsten.texifyidea.util.parser.traverseRequiredParams

/**
 * Fold sections, custom regions, begin-end environments, symbols, and footnotes in LaTeX documents.
 *
 * These folding are unified in a single folding builder to reduce the number of whole-tree traversals and improve performance.
 *
 * @author Li Ernest
 */
class LatexUnifiedFoldingBuilder : FoldingBuilderEx(), DumbAware {

    /**
     * A map of section commands (including `\`) to their levels.
     */
    private val sectionLevels: Map<String, Int> = CommandMagic.sectionNameToLevel

    /**
     * Implements custom folding regions.
     * See https://blog.jetbrains.com/idea/2012/03/custom-code-folding-regions-in-intellij-idea-111/
     *
     * Syntax:
     * %!<editor-fold desc="MyDescription">
     * %!</editor-fold>
     *
     * %!region MyDescription
     * %!endregion
     */
    private val startRegionRegex = """%!\s*((<editor-fold(\s+desc="(?<description>[^"]*)")?>)|(region(\s+(?<description2>.+))?))""".toRegex()
    private val endRegionRegex = """%!\s*(</editor-fold>|endregion)""".toRegex()

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        // We are guaranteed read access so we must not call any `runReadAction` here.
        val lookup = LatexDefinitionService.getBundleFor(root)
        val visitor = LatexFoldingVisitor(lookup)
        root.accept(visitor)
        visitor.endAll(root.endOffset)

        return visitor.descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String = node.text + "..."

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false

    private fun foldingDescriptorSection(element: PsiElement, range: TextRange): FoldingDescriptor {
        return foldingDescriptor(
            element, range,
            placeholderText = element.node.text + "...",
            isCollapsedByDefault = LatexCodeFoldingSettings.getInstance().foldSections
        )
    }

    private fun foldingDescriptorRegion(element: PsiElement, range: TextRange, name: String): FoldingDescriptor {
        return foldingDescriptor(
            element, range,
            placeholderText = name.ifEmpty { "..." },
            isCollapsedByDefault = false
        )
    }

    /**
     * Escape special symbols in LaTeX.
     */
    val escapedSymbols = setOf("%", "#", "&", "_", "$")

    private fun findCommandFoldedSymbol(nameWithSlash: String, lookup: LatexSemanticsLookup): String? {
        val nameWithoutSlash = nameWithSlash.removePrefix("\\")
        if (nameWithoutSlash in escapedSymbols) return nameWithoutSlash
        val cmd = lookup.lookupCommand(nameWithoutSlash) ?: return null
        return cmd.display
    }

    private fun foldingDescriptorSymbol(cmdToken: PsiElement, display: String): FoldingDescriptor {
        return foldingDescriptor(
            cmdToken, cmdToken.textRange,
            placeholderText = display,
            isCollapsedByDefault = LatexCodeFoldingSettings.getInstance().foldSymbols
        )
    }

    private fun foldingDescriptorEnvironment(o: LatexEnvironment, range: TextRange): FoldingDescriptor {
        return foldingDescriptor(
            o, range,
            placeholderText = "...",
            isCollapsedByDefault = LatexCodeFoldingSettings.getInstance().foldEnvironments
        )
    }

    /**
     * Minimum length of a footnote to fold.
     */
    private val minFootnoteLength = 8

    private fun foldingDescriptorFootnote(o: LatexCommands, range: TextRange): FoldingDescriptor {
        val parsedText = o.text.substring(1).trim()
        val placeHolderText = if (parsedText.length > minFootnoteLength) {
            parsedText.take(minFootnoteLength) + "..."
        }
        else {
            parsedText.dropLast(1)
        }
        return foldingDescriptor(
            o, range,
            placeholderText = placeHolderText,
            isCollapsedByDefault = LatexCodeFoldingSettings.getInstance().foldFootnotes
        )
    }

    private data class FoldingEntry(
        val command: PsiElement,
        val start: Int,
        val level: Int,
        val name: String
    )

    /**
     * We use this visitor to traverse all section commands and magic comments that define regions.
     */
    private inner class LatexFoldingVisitor(
        val lookup: LatexSemanticsLookup
    ) : LatexRecursiveVisitor() {
        /*
        Rules:

        section commands can be ended by a higher level section command or an endregion magic comment.
        region magic comments can only be ended by an endregion magic comment.
         */

        val descriptors = mutableListOf<FoldingDescriptor>()

        /**
         * The current section commands found in the document.
         */
        val sectionStack = ArrayDeque<FoldingEntry>()

        val regionStack = ArrayDeque<FoldingEntry>()

        /**
         * We have to keep track of the section commands that are interrupted by a region end, so that we can recover them later
         * if there are only whitespaces between the new section command and the custom region end.
         */
        val interruptedSections = mutableListOf<FoldingEntry>()
        var interruptingEnd: PsiElement? = null

        /**
         * The size of the section stack that should not be popped.
         */
        var prevLevelSize: Int = 0

        fun endSectionCommand(newSection: PsiElement, endLevel: Int) {
            if (sectionStack.size == prevLevelSize || endLevel > sectionStack.last().level) {
                // If the stack is (effectively) empty or the current command is at a deeper level than the last one, we do not pop
                return
            }
            val prev = newSection.prevContextualSiblingIgnoreWhitespace()
            val endOffset = prev?.endOffset ?: newSection.startOffset
            val lastRegionStart = regionStack.lastOrNull()?.start ?: -1
            while (sectionStack.size > prevLevelSize) {
                val foldingEntry = sectionStack.last()
                val lastCommand = foldingEntry.command
                val lastLevel = foldingEntry.level
                if (lastLevel < endLevel) {
                    break // The last command is at a lower level, stop popping
                }
                sectionStack.removeLast()
                val foldingRange = TextRange(lastCommand.startOffset, endOffset)
                if (foldingRange.startOffset >= lastRegionStart) {
                    descriptors.add(foldingDescriptorSection(lastCommand, foldingRange))
                }
            }
        }

        private fun encounterSectionCommand(s: FoldingEntry) {
            endInterruptedSectionCommand(s.command, s.level)
            interruptedSections.clear()
            interruptingEnd = null
            // clear the all the interrupted sections as they should be truly ended by a new section command or truly interrupted by some additional content

            endSectionCommand(s.command, s.level)
            sectionStack.addLast(s) // Add the current command back to the stack
        }

        private fun endInterruptedSectionCommand(command: PsiElement, endLevel: Int) {
            /*
            The sections, if not interrupted, should be ended here
             */
            if (interruptedSections.isEmpty()) return
            var endOffset = command.prevContextualSiblingIgnoreWhitespace()?.endOffset
            if (endOffset == null || descriptors.isEmpty() || endOffset > descriptors.last().range.endOffset) {
                return
            }
            // now we jump to the front of the interruptions
            endOffset = interruptingEnd?.prevContextualSiblingIgnoreWhitespace()?.endOffset ?: endOffset

            for (s in interruptedSections) {
                if (s.level >= endLevel) {
                    // these sections are not interrupted by the current command
                    val foldingRange = TextRange(s.command.startOffset, endOffset)
                    val descriptor = foldingDescriptorSection(s.command, foldingRange)
                    descriptors.add(descriptor)
                }
            }
        }

        private fun endRegionCommand(command: PsiElement) {
            // As the end region is found, we pop the last section command from the stack
            // and create a folding descriptor for it
            if (regionStack.isEmpty()) return
            val lastRegionEntry = regionStack.removeLast()
            val foldingRange = TextRange(lastRegionEntry.start, command.endOffset)
            val descriptor = foldingDescriptorRegion(lastRegionEntry.command, foldingRange, lastRegionEntry.name)
            descriptors.add(descriptor)

            // Additionally, we keep track of the last command that interrupted the section commands
            if (interruptingEnd == null) {
                interruptingEnd = command
            }
            while (sectionStack.size > prevLevelSize) {
                val s = sectionStack.last()
                if (s.start < foldingRange.startOffset) {
                    // it starts before the folding,
                    break
                }
                interruptedSections.addLast(sectionStack.removeLast())
            }
        }

        fun endAll(lastOffset: Int) {
            // End all sections that are still open
            while (sectionStack.size > prevLevelSize) {
                val s = sectionStack.removeLast()
                val foldingRange = TextRange(s.command.textOffset, lastOffset)
                val descriptor = foldingDescriptorSection(s.command, foldingRange)
                descriptors.add(descriptor)
            }
        }

        override fun visitCommands(o: LatexCommands) {
            /*
            \section, \subsection, etc. are section commands.
             */
            val element = o
            val name = element.name ?: return

            visitPossibleSectionCommand(element, name)
            visitPossibleSymbol(element, name)

            element.acceptChildren(this)
        }

        private fun visitPossibleSectionCommand(element: LatexCommands, name: String) {
            // fold section commands
            val level = sectionLevels[name]
            // If the command is likely in a command definition or in the preamble, skip it
            if (level != null && element.firstChild != null &&
                PsiTreeUtil.getParentOfType(element, LatexParameter::class.java) == null
            ) {
                encounterSectionCommand(FoldingEntry(element, element.startOffset, level, name))
            }
        }

        private fun visitPossibleSymbol(element: LatexCommands, name: String) {
            /*
            If the command is a math command, we add it to the stack.
             */
            val display = findCommandFoldedSymbol(name, lookup) ?: return
            val descriptor = foldingDescriptorSymbol(element.commandToken, display)
            descriptors.add(descriptor)
        }

        private fun visitPossibleFootnoteCommand(element: LatexCommands, name: String) {
            if (name in CommandMagic.foldableFootnotes) {
                element.traverseRequiredParams {
                    val textRange = it.textRange
                    // If the footnote has a required parameter, we fold it
                    if (textRange.length <= 2) {
                        return@traverseRequiredParams // Skip empty parameters like {}
                    }
                    val descriptor = foldingDescriptorFootnote(
                        element,
                        TextRange(textRange.startOffset + 1, textRange.endOffset - 1)
                    )
                    descriptors.add(descriptor)
                }
            }
        }

        override fun visitMagicComment(o: LatexMagicComment) {
            val text = o.text
            startRegionRegex.find(text)?.let { match ->
                val groups = match.groups
                val name = groups["description"]?.value ?: groups["description2"]?.value?.trim() ?: ""
                // Add a `\` to the name to match the section command format
                val level = sectionLevels["\\$name"]
                if (level != null) {
                    // we have to end the sections before
                    endSectionCommand(o, level) // pop the last section command
                }
                // add it as a region
                regionStack.addLast(FoldingEntry(o, o.startOffset, Int.MIN_VALUE, name))
                return
            }

            endRegionRegex.find(text)?.let { match ->
                endRegionCommand(o)
                return
            }
        }

        override fun visitEnvironment(o: LatexEnvironment) {
            val envStart = o.beginCommand.endOffset()
            val envEnd = o.endCommand?.textOffset ?: return
            if (envStart < envEnd) {
                val descriptor = foldingDescriptorEnvironment(o, TextRange(envStart, envEnd))
                descriptors.add(descriptor)
            }
            // We enter a new level with the environment, and we should not end previous commands
            // While this is only for the `document` command now, we reserve it here for possible future change
            val newLevel = o.getEnvironmentName() == EnvironmentNames.DOCUMENT
            if (newLevel) {
                val originalBaseCount = prevLevelSize
                prevLevelSize = sectionStack.size
                o.acceptChildren(this)
                val lastOffset = o.environmentContent?.noMathContentList?.lastOrNull()?.endOffset ?: envEnd
                endAll(lastOffset)
                prevLevelSize = originalBaseCount
            }
            else {
                o.acceptChildren(this)
            }
        }

        override fun visitNormalText(o: LatexNormalText) {
            return
        }

        override fun visitWhiteSpace(space: PsiWhiteSpace) {
            return
        }

        override fun visitElement(element: PsiElement) {
            ProgressIndicatorProvider.checkCanceled()
            element.acceptChildren(this)
        }

        override fun visitLeftRight(o: LatexLeftRight) {
            val descendants = o.descendants(canGoInside = { it == o }).toList()

            var leftDisplay: String

            var rightDisplay: String

            if (descendants[1].text.substring(1) in PredefinedCmdPairedDelimiters.delimiterLeftMap) {
                leftDisplay = PredefinedCmdPairedDelimiters.delimiterLeftMap[descendants[1].text.substring(1)]?.leftDisplay ?: descendants[1].text

                rightDisplay = PredefinedCmdPairedDelimiters.delimiterLeftMap[descendants[1].text.substring(1)]?.rightDisplay ?: descendants[descendants.size - 1].text
            } else if ((descendants[1].text + descendants[2].text).substring(1) in PredefinedCmdPairedDelimiters.delimiterLeftMap) {
                leftDisplay = PredefinedCmdPairedDelimiters.delimiterLeftMap[(descendants[1].text + descendants[2].text).substring(1)]?.leftDisplay ?: descendants[1].text

                rightDisplay = PredefinedCmdPairedDelimiters.delimiterLeftMap[(descendants[1].text + descendants[2].text).substring(1)]?.rightDisplay ?: descendants[descendants.size - 1].text
            } else {
                super.visitLeftRight(o)

                return
            }

            descriptors.add(
                foldingDescriptor(
                    o,
                    range = o.textRange,
                    placeholderText = leftDisplay + "..." + rightDisplay,
                    false
                )
            )

            super.visitLeftRight(o)
        }
    }
}