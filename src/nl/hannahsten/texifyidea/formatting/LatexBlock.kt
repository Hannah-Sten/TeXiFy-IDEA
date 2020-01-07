package nl.hannahsten.texifyidea.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.psi.impl.LatexEnvironmentImpl
import nl.hannahsten.texifyidea.util.requiredParameters
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
                        wrappingStrategy.getWrap(child),
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
        if (myNode.elementType === LatexTypes.ENVIRONMENT_CONTENT
                // Fix for leading comments inside an environment, because
                // somehow they are not placed inside environments.
                || myNode.elementType === LatexTypes.COMMENT_TOKEN
                && myNode.treeParent.elementType === LatexTypes.ENVIRONMENT) {

            val environment = myNode.treeParent.psi.originalElement as LatexEnvironmentImpl
            // Check if we are in a listings environment by checking the required
            // parameter of the begin command.
            return if (environment.beginCommand.requiredParameters().any { it.text.contains(DefaultEnvironment.LISTINGS.environmentName) }) {
                Indent.getAbsoluteNoneIndent()
            }
            else Indent.getNormalIndent(true)
        }

        // Indent content of groups. Not relative to their parent, because that
        // would be relative to the open brace of the group instead of the
        // (usually) command.
        if (myNode.elementType === LatexTypes.CONTENT
                && myNode.treeParent.elementType in setOf(LatexTypes.GROUP, LatexTypes.OPEN_GROUP)) {
            return Indent.getNormalIndent()
        }

        // Display math
        return if ((myNode.elementType === LatexTypes.MATH_CONTENT || myNode.elementType === LatexTypes.COMMENT_TOKEN)
                && myNode.treeParent.elementType === LatexTypes.DISPLAY_MATH) {
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