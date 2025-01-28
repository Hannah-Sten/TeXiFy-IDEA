package nl.hannahsten.texifyidea.formatting

import com.intellij.application.options.CodeStyle
import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.prevLeaf
import nl.hannahsten.texifyidea.editor.typedhandlers.LatexEnterHandler
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettings
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.firstChildOfType
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import java.lang.Integer.max

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
    val sectionIndent: Int = 0,
    /** Extra section indent that's not real but is used in case blocks do not start on a new line, so that it is ignored for the enter handler. */
    private val fakeSectionIndent: Int = 0,
) : AbstractBlock(node, wrap, alignment) {

    override fun buildChildren(): List<Block> {
        val blocks = mutableListOf<LatexBlock>()
        var child = myNode.firstChildNode

        // Only applicable for section indenting:
        // Current sectioning level while walking through the file
        // Uses levels of CommandMagic.labeledLevels
        var sectionLevel = -2
        // Extra indent to do because of sectioning
        var extraSectionIndent = max(sectionIndent - 1, 0)

        // If a block does not start on a new line the indent won't do anything and we need to do something else to get the text in the block indented
        val blockToIndentDoesNotStartOnNewLine = myNode.psi is LatexNoMathContent && sectionIndent > 0 && myNode.psi.prevLeaf(false)?.text?.contains("\n") == false

        // Sorry, it's magic
        val newFakeSectionIndent = if (blockToIndentDoesNotStartOnNewLine) sectionIndent + 1 else max(fakeSectionIndent - 1, 0)
        // (I think what it does is that it propagates the section indent to normal text words, and since it only does
        // something for things that come right after a new line the next normal text word on a new line will actually
        // be indented (and probably not so for other structures, but we have to choose a finite number >= 2 here))

        // For section indenting only: add fake blocks at the end because we can only add one indent per block but we may need multiple if inside e.g. subsubsection
        if (child == null && (sectionIndent > 0 || fakeSectionIndent > 0)) {
            val block = LatexBlock(
                myNode,
                wrappingStrategy.getNormalWrap(myNode),
                null,
                spacingBuilder,
                wrappingStrategy,
                extraSectionIndent,
                newFakeSectionIndent
            )
            blocks.add(block)
        }

        var isPreviousWhiteSpace = child != null && child.elementType !== TokenType.WHITE_SPACE && child !is PsiWhiteSpace
        // Create child blocks
        while (child != null) {
            val isSectionCommand =
                child.psi is LatexNoMathContent && child.psi.firstChildOfType(LatexCommands::class)?.name in CommandMagic.sectioningCommands.map { it.cmd }

            var targetIndent = extraSectionIndent

            if (isSectionCommand) {
                updateSectionIndent(sectionLevel, extraSectionIndent, targetIndent, child).apply {
                    sectionLevel = first
                    extraSectionIndent = second
                    targetIndent = third
                }
            }

            // Normal addition of blocks
            if (child.elementType !== TokenType.WHITE_SPACE && child !is PsiWhiteSpace) {
                val block = LatexBlock(
                    child,
                    // Only allow wrapping if the previous element is a white space.
                    if (isPreviousWhiteSpace) wrappingStrategy.getNormalWrap(myNode) else wrappingStrategy.getNoneWrap(),
                    null,
                    spacingBuilder,
                    wrappingStrategy,
                    targetIndent,
                    newFakeSectionIndent
                )
                blocks.add(block)
                isPreviousWhiteSpace = false
            }
            else isPreviousWhiteSpace = true
            child = child.treeNext
        }
        return blocks
    }

    /**
     * Update the current section indent based on the current child.
     * Only applicable when indenting text in sections.
     *
     * @return sectionLevel, extraSectionIndent, targetIndent
     */
    private fun updateSectionIndent(givenSectionLevel: Int, givenExtraSectionIndent: Int, givenTargetIndent: Int, child: ASTNode): Triple<Int, Int, Int> {
        var extraSectionIndent = givenExtraSectionIndent
        var sectionLevel = givenSectionLevel
        var targetIndent = givenTargetIndent

        // Set flag for next blocks until section end to get indent+1
        // We need to do it this way because we cannot create blocks which span a section content: blocks
        // need to correspond to only one psi element.
        // Changing the parser to view section content as one element is problematic because then we need to hardcode the sectioning structure in the parser
        val command = LatexCommand.lookup(child.psi.firstChildOfType(LatexCommands::class)?.name)?.first()
        val level = CommandMagic.labeledLevels[command]
        if (level != null && level > sectionLevel) {
            extraSectionIndent += 1
            sectionLevel = level
        }
        else if (level != null && level < sectionLevel) {
            // I think this will go wrong if you jump levels, e.g. subsubsection after chapter
            // but that's bad style anyway
            extraSectionIndent = max(targetIndent - (sectionLevel - level), 0)
            // Suppose previous text is indented 2 times, and we are a one level higher section command,
            // we need for this command itself an indent of 2, minus one for the level, minus one because the
            // section command itself is indented one less than the text in the section
            // (which will be indented with extraSectionIndent)
            targetIndent = max(targetIndent - (sectionLevel - level) - 1, 0)
            sectionLevel = level
        }
        else if (level != null) {
            // We encounter a same level sectioning command, which should be indented one less
            targetIndent -= 1
        }

        return Triple(sectionLevel, extraSectionIndent, targetIndent)
    }

    override fun getIndent(): Indent? {
        val shouldIndentDocumentEnvironment = CodeStyle.getCustomSettings(node.psi.containingFile, LatexCodeStyleSettings::class.java).INDENT_DOCUMENT_ENVIRONMENT
        val shouldIndentEnvironments = CodeStyle.getCustomSettings(node.psi.containingFile, LatexCodeStyleSettings::class.java).INDENT_ENVIRONMENTS
        val isDocumentEnvironment = myNode.elementType === LatexTypes.ENVIRONMENT_CONTENT &&
            (myNode.psi as LatexEnvironmentContent)
                .firstParentOfType(LatexEnvironment::class)
                ?.firstChildOfType(LatexBeginCommand::class)
                ?.firstChildOfType(LatexParameterText::class)?.text == "document"
        val shouldIndentEnvironment = when {
            myNode.elementType !== LatexTypes.ENVIRONMENT_CONTENT -> false
            isDocumentEnvironment -> shouldIndentDocumentEnvironment
            else -> shouldIndentEnvironments
        }

        if (shouldIndentEnvironment || myNode.elementType === LatexTypes.PSEUDOCODE_BLOCK_CONTENT || myNode.elementType === LatexTypes.IF_BLOCK_CONTENT ||
            // Fix for leading comments inside an environment, because somehow they are not placed inside environments.
            // Note that this does not help to insert the indentation, but at least the indent is not removed
            // when formatting.
            (
                myNode.elementType === LatexTypes.COMMENT_TOKEN &&
                    myNode.treeParent?.elementType === LatexTypes.ENVIRONMENT
                )
        ) {
            return Indent.getNormalIndent(true)
        }

        // Indentation of sections
        val indentSections = CodeStyle.getCustomSettings(node.psi.containingFile, LatexCodeStyleSettings::class.java).INDENT_SECTIONS
        if (indentSections) {
            if (sectionIndent > 0 || fakeSectionIndent > 0) {
                return Indent.getNormalIndent(false)
            }
        }

        // Indentation in groups and parameters.
        if (myNode.elementType === LatexTypes.REQUIRED_PARAM_CONTENT ||
            myNode.elementType === LatexTypes.STRICT_KEY_VAL_PAIR ||
            myNode.elementType === LatexTypes.OPTIONAL_KEY_VAL_PAIR ||
            (
                myNode.elementType !== LatexTypes.CLOSE_BRACE &&
                    myNode.treeParent?.elementType === LatexTypes.GROUP
                ) ||
            (
                myNode.elementType !== LatexTypes.CLOSE_BRACE &&
                    myNode.treeParent?.elementType === LatexTypes.PARAMETER_GROUP
                )
        ) {
            return Indent.getNormalIndent(false)
        }

        if (myNode.elementType == LatexTypes.LEFT_RIGHT_CONTENT) {
            return Indent.getNormalIndent(true)
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
        return myNode.firstChildNode == null && sectionIndent <= 0
    }

    // Automatic indent when enter is pressed
    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        return LatexEnterHandler.getChildAttributes(newChildIndex, node, subBlocks)
    }
}