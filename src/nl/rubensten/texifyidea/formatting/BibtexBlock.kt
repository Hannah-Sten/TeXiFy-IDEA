package nl.rubensten.texifyidea.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import nl.rubensten.texifyidea.psi.BibtexId
import nl.rubensten.texifyidea.psi.BibtexPreamble
import nl.rubensten.texifyidea.psi.BibtexTypes
import nl.rubensten.texifyidea.util.hasParent
import nl.rubensten.texifyidea.util.previousSiblingIgnoreWhitespace

/**
 * @author Ruben Schellekens
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
        val psi = myNode.psi

        // Indent tags.
        if (type == BibtexTypes.TAG) {
            return Indent.getNormalIndent(false)
        }
        // Prevent close brace indent.
        if (type == BibtexTypes.PREAMBLE) {
            return Indent.getNormalIndent(false)
        }

        return Indent.getNoneIndent()
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        val type = myNode.elementType
        val psi = myNode.psi

        if (psi.previousSiblingIgnoreWhitespace() is BibtexId) {
            return ChildAttributes(Indent.getNormalIndent(true), null)
        }
        if (psi.hasParent(BibtexPreamble::class)) {
            return ChildAttributes(Indent.getNormalIndent(true), null)
        }

        return ChildAttributes(Indent.getNoneIndent(), null)
    }
}