package nl.hannahsten.texifyidea.editor.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.lang.commands.LatexMathCommand
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.endOffset
import nl.hannahsten.texifyidea.util.parser.parentOfType
import nl.hannahsten.texifyidea.util.parser.previousSiblingIgnoreWhitespace
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
    private val sectionLevels: Map<String, Int> = CommandMagic.sectioningCommands.mapIndexed { index, command -> "\\${command.command}" to index }.toMap()

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
        /*
        We are guaranteed read access so we must not call any `runReadAction` here.
         */
        val visitor = LatexFoldingVisitor()
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
     * A map of math commands (including `\`) to their folded symbols.
     * The keys are the commands with a backslash, e.g. `\alpha`, and the values are the symbols, e.g. `α`.
     */
    private val commandToFoldedSymbol: Map<String, String> = buildMap {
        val escapedSymbols = listOf("%", "#", "&", "_", "$")
        for (s in escapedSymbols) {
            put("\\$s", s) // e.g. \% -> %
        }
        for (cmd in LatexMathCommand.values()) {
            val display = cmd.display ?: continue
            putIfAbsent(cmd.commandWithSlash, display) // e.g. \alpha -> α
        }
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
            parsedText.substring(0, minFootnoteLength) + "..."
        }
        else {
            parsedText.substring(0, parsedText.length - 1)
        }
        return foldingDescriptor(
            o, range,
            placeholderText = placeHolderText,
            isCollapsedByDefault = LatexCodeFoldingSettings.getInstance().foldFootnotes
        )
    }

    private data class FoldingEntry(
        val command: PsiElement,
        val level: Int,
        val name: String
    )

    /**
     * We use this visitor to traverse all section commands and magic comments that define regions.
     */
    private inner class LatexFoldingVisitor : LatexRecursiveIgnoreTextVisitor() {
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

        /**
         * The count of ignored folding entries as the base
         */
        var baseCount : Int = 0

        private fun addSectionCommand(s: FoldingEntry, endLevel: Int) {
            if (sectionStack.size == baseCount) {
                // If the stack is (effectively) empty, we can add it directly
                sectionStack.addLast(s)
                return
            }
            // If the current command is at a deeper level than the last one, push it onto the stack
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
            while (sectionStack.size > baseCount) {
                val (lastCommand, lastLevel) = sectionStack.last()
                if (lastLevel < level) {
                    break // The last command is at a lower level, stop popping
                }
                sectionStack.removeLast()
                val foldingRange = TextRange(lastCommand.startOffset, endOffset)
                descriptors.add(foldingDescriptorSection(lastCommand, foldingRange))
            }
        }

        private fun endRegionCommand(command: PsiElement) {
            val endOffset = command.endOffset
            while (sectionStack.size > baseCount) {
                // ignore other section commands and find the last region command, whose level must be Int.MIN_VALUE
                val s = sectionStack.removeLast()
                if (s.level != Int.MIN_VALUE) {
                    continue
                }
                val lastCommand = s.command
                val foldingRange = TextRange(lastCommand.startOffset, endOffset)
                val descriptor = foldingDescriptorRegion(lastCommand, foldingRange, s.name)
                descriptors.add(descriptor)
                break
            }
        }

        fun endAll(lastOffset: Int) {
            // End all sections that are still open
            while (sectionStack.size > baseCount) {
                val s = sectionStack.removeLast()
                if (s.level == Int.MIN_VALUE) {
                    // skip unclosed region commands
                    continue
                }
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
            /*
            If the command is a section command, we add it to the stack.
            If the command is not a section command, we end all sections that are still open.
             */
            // fold section commands
            val level = sectionLevels[name]
            // If the command is likely in a command definition or in the preamble, skip it
            if (level != null && element.firstChild != null &&
                PsiTreeUtil.getParentOfType(element, LatexParameter::class.java) == null
            ) {
                addSectionCommand(FoldingEntry(element, level, name), endLevel = level)
            }
        }

        private fun visitPossibleSymbol(element: LatexCommands, name: String) {
            /*
            If the command is a math command, we add it to the stack.
            If the command is not a math command, we end all sections that are still open.
             */
            val display = commandToFoldedSymbol[name] ?: return
            val descriptor = foldingDescriptorSymbol(element.commandToken, display)
            descriptors.add(descriptor)
        }

        private fun visitPossibleFootnoteCommand(element: LatexCommands, name: String) {
            if (name in CommandMagic.foldableFootnotes) {
                element.traverseRequiredParams {
                    val textRange = it.textRange
                    // If the footnote has a required parameter, we fold it
                    if (textRange.length <= 2) {
                        return@traverseRequiredParams true // Skip empty parameters like {}
                    }
                    val descriptor = foldingDescriptorFootnote(
                        element,
                        TextRange(textRange.startOffset + 1, textRange.endOffset - 1)
                    )
                    descriptors.add(descriptor)

                    true
                }
            }
        }

        override fun visitMagicComment(o: LatexMagicComment) {
            val text = o.text
            startRegionRegex.find(text)?.let { match ->
                val groups = match.groups
                val name = groups["description"]?.value ?: groups["description2"]?.value?.trim() ?: ""
                // Add a `\` to the name to match the section command format
                val endLevel = sectionLevels["\\$name"] ?: Int.MAX_VALUE // custom region commands do not terminate sections
                val s = FoldingEntry(o, Int.MIN_VALUE, name)
                addSectionCommand(s, endLevel)
                return
            }

            endRegionRegex.find(text)?.let { match ->
                // If the end region is found, we pop the last section command from the stack
                // and create a folding descriptor for it
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
            val newLevel = o.getEnvironmentName() == DefaultEnvironment.DOCUMENT.environmentName
            if (newLevel) {
                val originalBaseCount = baseCount
                baseCount = sectionStack.size
                o.acceptChildren(this)
                val lastOffset = o.environmentContent?.noMathContentList?.lastOrNull()?.endOffset ?: envEnd
                endAll(lastOffset)
                baseCount = originalBaseCount
            }else{
                o.acceptChildren(this)
            }
        }
    }
}