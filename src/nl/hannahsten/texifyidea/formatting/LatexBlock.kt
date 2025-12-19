package nl.hannahsten.texifyidea.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.prevLeaf
import nl.hannahsten.texifyidea.editor.typedhandlers.LatexEnterHandler
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.lang.predefined.EnvironmentNames
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettings
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType
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
    val settings: CodeStyleSettings,
    val sectionIndent: Int = 0,
    /** Extra section indent that's not real but is used in case blocks do not start on a new line, so that it is ignored for the enter handler. */
    private val fakeSectionIndent: Int = 0,
) : AbstractBlock(node, wrap, alignment) {

    override fun buildChildren(): List<Block> {
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
                LatexWrappingStrategy.getNormalWrap(settings, myNode),
                null,
                spacingBuilder,
                settings,
                extraSectionIndent,
                newFakeSectionIndent
            )
            return listOf(block)
        }

        val blocks = mutableListOf<Block>()

        var isPreviousWhiteSpace = child != null && child.elementType !== TokenType.WHITE_SPACE && child !is PsiWhiteSpace
        // Create child blocks
        while (child != null) {
            val psi = child.psi
            val isSectionCommand = psi is LatexNoMathContent && psi.commands?.name in CommandMagic.sectionNameToLevel

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
                    if (isPreviousWhiteSpace) LatexWrappingStrategy.getNormalWrap(settings, myNode) else LatexWrappingStrategy.getNoneWrap(),
                    null,
                    spacingBuilder,
                    settings,
                    targetIndent,
                    newFakeSectionIndent
                )
                if(child.elementType == LatexTypes.BEGIN_COMMAND) {
                    blocks.addAll(block.subBlocks)
                } else {
                    blocks.add(block)
                }
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
        val command = child.psi.findFirstChildOfType(LatexCommands::class)?.nameWithSlash
        val level = CommandMagic.sectionNameToLevel[command]
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
        val latexSettings = settings.getCustomSettings(LatexCodeStyleSettings::class.java)
        val shouldIndentDocumentEnvironment = latexSettings.INDENT_DOCUMENT_ENVIRONMENT
        val shouldIndentEnvironments = latexSettings.INDENT_ENVIRONMENTS
        val elementType = myNode.elementType
        val isDocumentEnvironment = elementType === LatexTypes.ENVIRONMENT_CONTENT &&
            (myNode.psi as LatexEnvironmentContent)
                .firstParentOfType(LatexEnvironment::class)?.getEnvironmentName() == EnvironmentNames.DOCUMENT
        val shouldIndentEnvironment = when {
            elementType !== LatexTypes.ENVIRONMENT_CONTENT -> false
            isDocumentEnvironment -> shouldIndentDocumentEnvironment
            else -> shouldIndentEnvironments
        }

        if (shouldIndentEnvironment || elementType === LatexTypes.PSEUDOCODE_BLOCK_CONTENT || elementType === LatexTypes.IF_BLOCK_CONTENT ||
            // Fix for leading comments inside an environment, because somehow they are not placed inside environments.
            // Note that this does not help to insert the indentation, but at least the indent is not removed
            // when formatting.
            (
                elementType === LatexTypes.COMMENT_TOKEN &&
                    myNode.treeParent?.elementType === LatexTypes.ENVIRONMENT
                )
        ) {
            return Indent.getNormalIndent(true)
        }

        // Workaround for fake parameter case below, to fix formatting of elements in the block which does not start on a newline due to starting right after the fake parameter
        // For children of environment, or environment content contains normal text and then other blocks in which case we check if we are in the first block
        if (myNode.psi.parent is LatexEnvironmentContent || ((myNode.psi.firstParentOfType<LatexEnvironmentContent>() as? LatexEnvironmentContent)?.firstChild?.firstChild == myNode.psi.parent)) {
            val environmentContent = myNode.psi.firstParentOfType<LatexEnvironmentContent>()
            // Check if there is a newline before the environment content starts: if so, we don't need to correct anything
            if (environmentContent?.prevSibling is LatexBeginCommand || (environmentContent?.prevSibling is PsiWhiteSpace && environmentContent.prevSibling.text?.contains("\n") != true)) {
                // Since the (first block in the) environment content does not start on a newline, we need to indent manually
                return Indent.getNormalIndent(false)
            }
        }

        // for mistakenly parsed parameters, such as \begin{equation} [x+y]^2 \end{equation}, we use semantics to decide whether to indent or not
        if (shouldIndentEnvironments && elementType === LatexTypes.PARAMETER) {
            val parameter = myNode.psi as? LatexParameter
            val beginElement = parameter?.parent as? LatexBeginCommand
            val envElement = beginElement?.parent as? LatexEnvironment
            if (envElement != null) {
                if (envElement.getEnvironmentName() == EnvironmentNames.DOCUMENT) {
                    // document environment do not have parameters, so always indent according to settings
                    return if (shouldIndentDocumentEnvironment) Indent.getNormalIndent(true) else Indent.getNoneIndent()
                }

                val semantics = LatexDefinitionService.resolveEnv(envElement)
                if (semantics != null) {
                    val arg = LatexPsiUtil.getCorrespondingArgument(beginElement, parameter, semantics.arguments)
                    // if the argument is null, it means it's a mistakenly parsed parameter, so we indent according to settings
                    return if (arg == null) Indent.getNormalIndent(true) else Indent.getNoneIndent()
                }
            }
        }

        // Indentation of sections
        val indentSections = latexSettings.INDENT_SECTIONS
        if (indentSections) {
            if (sectionIndent > 0 || fakeSectionIndent > 0) {
                return Indent.getNormalIndent(false)
            }
        }

        // Indentation in groups and parameters.
        if (elementType === LatexTypes.REQUIRED_PARAM_CONTENT ||
            elementType === LatexTypes.STRICT_KEY_VAL_PAIR ||
            elementType === LatexTypes.OPTIONAL_KEY_VAL_PAIR ||
            (
                elementType !== LatexTypes.CLOSE_BRACE &&
                    myNode.treeParent?.elementType === LatexTypes.GROUP
                ) ||
            (
                elementType !== LatexTypes.CLOSE_BRACE &&
                    myNode.treeParent?.elementType === LatexTypes.PARAMETER_GROUP
                )
        ) {
            return Indent.getNormalIndent(false)
        }

        if (elementType == LatexTypes.LEFT_RIGHT_CONTENT) {
            return Indent.getNormalIndent(true)
        }

        // Display math
        return if ((elementType === LatexTypes.MATH_CONTENT || elementType === LatexTypes.COMMENT_TOKEN) &&
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