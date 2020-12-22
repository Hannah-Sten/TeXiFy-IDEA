package nl.hannahsten.texifyidea.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import nl.hannahsten.texifyidea.psi.LatexTypes
import java.util.*

/**
 * @author Sten Wessel
 */
class LatexBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val spacingBuilder: TexSpacingBuilder,
    private val wrappingStrategy: LatexWrappingStrategy
) : AbstractBlock(node, wrap, alignment) {

    override fun buildChildren(): List<Block> {
        val blocks: MutableList<Block> = ArrayList()
        var child = myNode.firstChildNode

        while (child != null) {
            if (child.elementType !== TokenType.WHITE_SPACE) {
                val block: Block = LatexBlock(
                    child,
                    wrappingStrategy.getWrap(),
                    null,
                    spacingBuilder,
                    wrappingStrategy
                )
                blocks.add(block)
            }
            child = child.treeNext
        }
        return blocks
    }

    override fun getIndent(): Indent? {
        if (myNode.elementType === LatexTypes.ENVIRONMENT_CONTENT ||
            myNode.elementType === LatexTypes.PSEUDOCODE_BLOCK_CONTENT ||
            // Fix for leading comments inside an environment, because somehow they are not placed inside environments.
            // Note that this does not help to insert the indentation, but at least the indent is not removed
            // when formatting.
            (myNode.elementType === LatexTypes.COMMENT_TOKEN &&
                myNode.treeParent?.elementType === LatexTypes.ENVIRONMENT)
        ) {
            return Indent.getNormalIndent(true)
        }

        // Indentation in groups and parameters.
        if (myNode.elementType === LatexTypes.REQUIRED_PARAM_CONTENT ||
            myNode.elementType === LatexTypes.OPTIONAL_PARAM_CONTENT ||
            myNode.elementType === LatexTypes.KEYVAL_PAIR ||
            (myNode.elementType !== LatexTypes.CLOSE_BRACE &&
                myNode.treeParent?.elementType === LatexTypes.GROUP)
        ) {
            return Indent.getNormalIndent(false)
        }

        // Display math
        return if ((myNode.elementType === LatexTypes.MATH_CONTENT || myNode.elementType === LatexTypes.COMMENT_TOKEN) &&
            myNode.treeParent?.elementType === LatexTypes.DISPLAY_MATH
        ) {
            Indent.getNormalIndent(true)
        }
        else Indent.getNoneIndent()
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spacingBuilder.getSpacing(this, child1, child2)
    }

    override fun isLeaf(): Boolean {
        return myNode.firstChildNode == null
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        val type = myNode.elementType
        if (type === LatexTypes.DISPLAY_MATH) {
            return ChildAttributes(Indent.getNormalIndent(true), null)
        }
        else if (type === LatexTypes.ENVIRONMENT) {
            return ChildAttributes(Indent.getNormalIndent(true), null)
        }

        return ChildAttributes(Indent.getNoneIndent(), null)
    }
}