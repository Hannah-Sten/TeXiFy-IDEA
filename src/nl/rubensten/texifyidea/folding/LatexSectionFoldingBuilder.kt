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

    override fun buildFoldRegions(root: PsiElement, document: Document,
                                  quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        val commands = LatexCommandsIndex.getCommandsByNames(root.containingFile, *sectionCommands).toList()
                .sortedBy { it.textOffset }
        if (commands.isEmpty()) {
            return descriptors.toTypedArray()
        }

        for (index_cur in 0 until commands.size) {
            var found_higher = false
            val cur_command = commands[index_cur]
            val depth_cur = sectionCommands.indexOf(cur_command.name)
            for (index_next in index_cur + 1 until commands.size) {
                val next_command = commands[index_next]
                val depth_next = sectionCommands.indexOf(next_command.name)
                if (depth_cur >= depth_next) {
                    val end = next_command.parentOfType(LatexContent::class)
                            ?.previousSiblingIgnoreWhitespace() ?: break
                    val folding_range = TextRange(cur_command.textOffset, end.textOffset + end.textLength)
                    if (folding_range.length > 0) {
                        descriptors.add(FoldingDescriptor(cur_command, folding_range))
                    }
                    found_higher = true
                    break
                }
            }
            /* If this item is the topmost level of the section structure or the last section marker,
             * use the end of all text as the end of the range.
             */
            if (!found_higher) {
                val content = commands.last().parentOfType(LatexContent::class) ?: continue // TODO: Check this
                var next: LatexContent? = content
                var previous: LatexContent = content
                while (next != null) {
                    previous = next
                    next = next.nextSiblingIgnoreWhitespace() as? LatexContent
                }
                val range = TextRange(cur_command.textOffset, previous.textOffset + previous.textLength)
                if (range.length > 0) {
                    descriptors.add(FoldingDescriptor(cur_command, range))
                }
            }
        }
        return descriptors.toTypedArray()
    }
}
