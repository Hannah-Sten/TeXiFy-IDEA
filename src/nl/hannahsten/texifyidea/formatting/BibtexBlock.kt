package nl.hannahsten.texifyidea.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import nl.hannahsten.texifyidea.psi.BibtexTypes

/**
 * @author Hannah Schellekens
 */
open class BibtexBlock(
        node: ASTNode,
        wrap: Wrap,
        alignment: Alignment?,
        val spacingBuilder: SpacingBuilder
) : AbstractBlock(node, wrap, alignment) {

    override fun buildChildren(): MutableList<Block> {
        val blocks = ArrayList<Block>()
        var child = myNode.firstChildNode

        while (child != null) {
            if (child.elementType != TokenType.WHITE_SPACE) {
                val block = BibtexBlock(
                        child,
                        Wrap.createWrap(WrapType.NONE, false),
                        null,
                        spacingBuilder
                )
                blocks.add(block)
            }

            child = child.treeNext
        }

        return blocks
    }

    override fun getSpacing(child1: Block?, child2: Block) = spacingBuilder.getSpacing(this, child1, child2)

    override fun isLeaf() = myNode.firstChildNode == null

    override fun getIndent(): Indent? {
        val type = myNode.elementType

        // Prevent close brace indent.
        if (type == BibtexTypes.PREAMBLE) {
            return Indent.getNormalIndent(false)
        }
        // Indents in entries.
        if (type == BibtexTypes.ENTRY_CONTENT) {
            return Indent.getNormalIndent(true)
        }

        return Indent.getNoneIndent()
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        val type = myNode.elementType

        if (type == BibtexTypes.ENTRY) {
            return ChildAttributes(Indent.getNormalIndent(true), null)
        }

        return ChildAttributes(Indent.getNoneIndent(), null)
    }
}