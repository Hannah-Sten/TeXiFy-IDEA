package nl.hannahsten.texifyidea.formatting

import com.intellij.application.options.CodeStyle
import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNoMathContent
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettings
import nl.hannahsten.texifyidea.util.firstChildOfType
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import java.lang.Integer.max
import java.util.*

/**
 * @author Sten Wessel
 *
 * @param sectionIndent Number of extra indents needed because of section indenting.
 */
class LatexBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val spacingBuilder: TexSpacingBuilder,
    private val wrappingStrategy: LatexWrappingStrategy,
    private val sectionIndent: Int = 0,
) : AbstractBlock(node, wrap, alignment) {

    override fun buildChildren(): List<Block> {
        val blocks = mutableListOf<LatexBlock>()
        var child = myNode.firstChildNode

        // Current sectioning level while walking through the file
        // Uses levels of CommandMagic.labeledLevels
        var sectionLevel = -2
        // Extra indent to do because of sectioning
        var extraSectionIndent = max(sectionIndent - 1, 0)

        // Add fake blocks at the end because we can only add one indent per block but we may need multiple if inside e.g. subsubsection
        if (child == null && sectionIndent > 0) {
            val block = LatexBlock(
                myNode,
                wrappingStrategy.getWrap(),
                null,
                spacingBuilder,
                wrappingStrategy,
                extraSectionIndent
            )
            blocks.add(block)
        }

        // todo idea: if we need to have say extra indent of 3 for this block
        //      then create two dummy children with indent
        while (child != null) {
            val isSectionCommand = child.psi is LatexNoMathContent && child.psi.firstChildOfType(LatexCommands::class)?.name in CommandMagic.labeledLevels.keys.map { it.cmd }

            var targetIndent = extraSectionIndent

            if (isSectionCommand) {
                // set flag for next blocks until section end to get indent+1
                val command = LatexCommand.lookup(child.psi.firstChildOfType(LatexCommands::class)?.name)?.firstOrNull()
                val level = CommandMagic.labeledLevels[command]
                if (level != null && level > sectionLevel) {
                    extraSectionIndent += 1
                    sectionLevel = level
                }
                else if (level != null && level < sectionLevel) {
                    // I think this will go wrong if you jump levels, e.g. subsubsection after chapter
                    // but that's bad style anyway
                    val difference = sectionLevel - level
                    extraSectionIndent = max(targetIndent - difference, 0)
                    targetIndent = max(targetIndent - difference, 0)
                    sectionLevel = level
                }
                else if (level != null) {
                    // We encounter a same level sectioning command, which should be indented one less
                    targetIndent -= 1
                }
            }
            if (child.elementType !== TokenType.WHITE_SPACE) {
                val block = LatexBlock(
                    child,
                    wrappingStrategy.getWrap(),
                    null,
                    spacingBuilder,
                    wrappingStrategy,
                    targetIndent
                )
                blocks.add(block)
            }
            child = child.treeNext
        }
        return blocks
    }

    override fun getIndent(): Indent? {
        if (sectionIndent > 0) {
            return Indent.getNormalIndent(false)
        }

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

        val indentSections = CodeStyle.getCustomSettings(node.psi.containingFile, LatexCodeStyleSettings::class.java).INDENT_SECTIONS
        if (indentSections) {
            if (myNode.elementType == LatexTypes.NORMAL_TEXT) {
                return Indent.getNormalIndent(false)
            }
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