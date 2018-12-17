package nl.rubensten.texifyidea.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexContent
import nl.rubensten.texifyidea.psi.PsiContainer
import nl.rubensten.texifyidea.util.Magic.Command.sectionMarkers
import nl.rubensten.texifyidea.util.nextSiblingIgnoreWhitespace
import nl.rubensten.texifyidea.util.parentOfType

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
        val commands = LatexCommandsIndex.getCommandsByNames(root.containingFile, *sectionCommands).toList()

        if (commands.isEmpty()) {
            return descriptors.toTypedArray()
        }

        // Fold all section markers.
        val index_last = recursiveFold(descriptors, commands, 0, commands.size)
        findEnd(descriptors, commands[index_last])
        return descriptors.toTypedArray()
    }

    private fun recursiveFold(descriptors: MutableCollection<FoldingDescriptor>, commands: List<LatexCommands>,
                              index_cur_toplevel: Int, index_last: Int): Int {
        if (index_cur_toplevel >= index_last) {
            return index_last - 1
        }
        val toplevel = commands[index_cur_toplevel]
        var index_next_toplevel = index_last
        for (i in index_cur_toplevel + 1 until index_last) {
            if (commands[i].name == toplevel.name) {
                index_next_toplevel = i
                break
            }
        }
        val end = commands[index_next_toplevel - 1]
        val elt = PsiContainer(toplevel, end)
        descriptors.add(FoldingDescriptor(elt, elt.textRange))
        if (index_cur_toplevel + 1 < index_next_toplevel) {
            recursiveFold(descriptors, commands, index_cur_toplevel + 1, index_next_toplevel)
        }
        return recursiveFold(descriptors, commands, index_next_toplevel, index_last)
    }

    private fun findEnd(descriptors: MutableCollection<FoldingDescriptor>, lastCommand: LatexCommands) {
        val content = lastCommand.parentOfType(LatexContent::class) ?: return
        var next: LatexContent? = content
        var previous: LatexContent = content
        while (next != null) {
            previous = next
            next = next.nextSiblingIgnoreWhitespace() as? LatexContent
        }

        val elt = PsiContainer(content, previous)
        descriptors.add(FoldingDescriptor(elt, elt.textRange))
    }
}