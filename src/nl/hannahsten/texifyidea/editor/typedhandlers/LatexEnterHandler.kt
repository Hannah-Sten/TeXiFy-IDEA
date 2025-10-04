package nl.hannahsten.texifyidea.editor.typedhandlers

import com.intellij.application.options.CodeStyle
import com.intellij.formatting.Block
import com.intellij.formatting.ChildAttributes
import com.intellij.formatting.Indent
import com.intellij.lang.ASTNode
import nl.hannahsten.texifyidea.formatting.LatexBlock
import nl.hannahsten.texifyidea.lang.predefined.EnvironmentNames
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettings

/**
 * Configure the indent after pressing enter.
 *
 * Technically, this is not a typed handler, but it's easier to find this way.
 */
object LatexEnterHandler {

    fun getChildAttributes(newChildIndex: Int, node: ASTNode, subBlocks: MutableList<Block>): ChildAttributes {
        val shouldIndentDocumentEnvironment = CodeStyle.getCustomSettings(node.psi.containingFile, LatexCodeStyleSettings::class.java).INDENT_DOCUMENT_ENVIRONMENT
        val shouldIndentEnvironments = CodeStyle.getCustomSettings(node.psi.containingFile, LatexCodeStyleSettings::class.java).INDENT_ENVIRONMENTS
        val isDocumentEnvironment = node.elementType === LatexTypes.ENVIRONMENT && (node.psi as? LatexEnvironment)?.getEnvironmentName() == EnvironmentNames.DOCUMENT
        val shouldIndentEnvironment = when {
            node.elementType !== LatexTypes.ENVIRONMENT -> false
            isDocumentEnvironment -> shouldIndentDocumentEnvironment
            else -> shouldIndentEnvironments
        }

        val type = node.elementType
        if (type == LatexTypes.DISPLAY_MATH || shouldIndentEnvironment || type == LatexTypes.LEFT_RIGHT) {
            return ChildAttributes(Indent.getNormalIndent(true), null)
        }

        val indentSections = CodeStyle.getCustomSettings(node.psi.containingFile, LatexCodeStyleSettings::class.java).INDENT_SECTIONS
        if (indentSections) {
            // This function will be called on the block for which the caret is adding something in the children at the given index,
            // however this may mean that the current block may be Content and a block will be added in the last child of the children of this block (so not directly into the children of this block).
            // Therefore to find the section indent of the line the caret was on, we need the previous leaf in the tree
            var currentBlock = subBlocks.getOrNull(newChildIndex - 1)
            var indentSize = (currentBlock as? LatexBlock)?.sectionIndent ?: 0
            while (currentBlock != null && !currentBlock.isLeaf) {
                currentBlock = currentBlock.subBlocks.lastOrNull()
                if (currentBlock is LatexBlock) {
                    indentSize = Integer.max(indentSize, currentBlock.sectionIndent)
                }
            }

            if (indentSize > 0) {
                // We may need to provide more than one indent, because of the problem that the parent-child relationship
                // does not match the indent sizes
                val singleIndentSize = CodeStyle.getIndentSize(node.psi.containingFile)
                return ChildAttributes(Indent.getSpaceIndent(indentSize * singleIndentSize), null)
            }
        }
        return ChildAttributes(Indent.getNoneIndent(), null)
    }
}