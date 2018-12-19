package nl.rubensten.texifyidea.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexContent
import nl.rubensten.texifyidea.psi.PsiContainer
import nl.rubensten.texifyidea.util.endOffset
import nl.rubensten.texifyidea.util.nextSiblingIgnoreWhitespace
import nl.rubensten.texifyidea.util.parentOfType
import nl.rubensten.texifyidea.util.previousSiblingIgnoreWhitespace

/**
 * Folds multiple \\usepackage or \\RequirePackage statements
 *
 * @author Ruben Schellekens
 */
open class LatexSectionFoldingBuilder : FoldingBuilderEx() {
    companion object {
        private val sectionCommands = arrayOf("\\part", "\\chapter",
                "\\section", "\\subsection", "\\subsubsection",
                "\\paragraph", "\\subparagraph")
    }


    override fun isCollapsedByDefault(node: ASTNode) = false

    override fun getPlaceholderText(node: ASTNode) = node.text + "..."

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        val commands = LatexCommandsIndex.getCommandsByNames(root.containingFile, *sectionCommands)
                .toList().sortedBy { x -> x.textOffset }

        if (commands.isEmpty()) {
            return descriptors.toTypedArray()
        }

        // Fold all section markers.
        recursiveFold(descriptors, commands, 0, commands.size)
        return descriptors.toTypedArray()
    }

    private fun recursiveFold(descriptors: MutableCollection<FoldingDescriptor>, commands: List<LatexCommands>,
                              index_cur_toplevel: Int, index_last: Int) {
        if (index_cur_toplevel >= index_last)
            return

        val toplevel = commands[index_cur_toplevel]
        var index_next_toplevel = index_last
        for (i in index_cur_toplevel + 1 until index_last) {
            if (commands[i].name == toplevel.name) {
                index_next_toplevel = i
                break
            }
        }
        val startOffset = toplevel.textOffset
        val endOffset: Int
        val lastFoldedCommand = commands[index_next_toplevel - 1]
        if (index_next_toplevel == commands.size) {
            val content = lastFoldedCommand.parentOfType(LatexContent::class) ?: return
            var next: LatexContent? = content
            var previous: LatexContent = content
            while (next != null) {
                previous = next
                next = next.nextSiblingIgnoreWhitespace() as? LatexContent
            }
            endOffset = previous.textOffset + previous.textLength
        } else {
            endOffset = lastFoldedCommand.textOffset + lastFoldedCommand.textLength
            System.err.println(lastFoldedCommand.name)
            System.err.println(lastFoldedCommand.textLength.toString())
        }


        if (startOffset > endOffset) {
            throw Exception("Startoffset = " + startOffset.toString() +
                    "\nendoffset = " + endOffset.toString() +
                    "\ncur = " + index_cur_toplevel.toString() + " (" + toplevel.name + ")" +
                    "\nindex_next = " + index_next_toplevel.toString() + " (" + commands[index_next_toplevel].name + ")" +
                    "\nindex_last = " + index_last.toString() +
                    "\nCommand queue:" + commands.map { x -> x.name }.toString())
        }

        if (endOffset > startOffset) {
            descriptors.add(FoldingDescriptor(toplevel.node, TextRange(startOffset, endOffset)))
        }

        if (index_cur_toplevel + 1 < index_next_toplevel) {
            recursiveFold(descriptors, commands, index_cur_toplevel + 1, index_next_toplevel)
        }
        recursiveFold(descriptors, commands, index_next_toplevel, index_last)
    }
}