package nl.hannahsten.texifyidea.formatting

import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Spacing
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import nl.hannahsten.texifyidea.formatting.spacingrules.leftTableSpaceAlign
import nl.hannahsten.texifyidea.formatting.spacingrules.rightTableSpaceAlign
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.psi.LatexTypes.*
import nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettings
import nl.hannahsten.texifyidea.util.labels.isLabelCommand
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.parser.inDirectEnvironment
import nl.hannahsten.texifyidea.util.parser.parentOfType

fun createSpacing(minSpaces: Int, maxSpaces: Int, minLineFeeds: Int, keepLineBreaks: Boolean, keepBlankLines: Int): Spacing =
    Spacing.createSpacing(minSpaces, maxSpaces, minLineFeeds, keepLineBreaks, keepBlankLines)

/**
 *
 * @author Sten Wessel, Abby Berkers
 */
fun createSpacingBuilder(settings: CodeStyleSettings): TexSpacingBuilder {
    val latexSettings = settings.getCustomSettings(LatexCodeStyleSettings::class.java)
    val latexCommonSettings = settings.getCommonSettings(LatexLanguage)

    return rules(latexCommonSettings) {
        custom {
            customRule { parent, left, right ->
                // Don't insert or remove spaces inside the text in a verbatim environment.
                if (parent.node?.elementType === NORMAL_TEXT) {
                    if (parent.node?.psi?.inDirectEnvironment(EnvironmentMagic.verbatim) == true) {
                        return@customRule Spacing.getReadOnlySpacing()
                    }
                }
                // Don't insert or remove spaces in front of the first word in a verbatim environment.
                else if (right.node?.elementType === ENVIRONMENT_CONTENT) {
                    if (right.node?.psi?.inDirectEnvironment(EnvironmentMagic.verbatim) == true) {
                        return@customRule Spacing.getReadOnlySpacing()
                    }
                    // We make an exception for the \label command: it makes sense to put it on the same line as the \begin, so we don't force it to the next line
                    if ((right.node?.psi?.firstChild?.firstChild as? LatexCommands)?.let { it.isLabelCommand(LatexDefinitionService.getBundleFor(it)) } == true) {
                        return@customRule Spacing.getReadOnlySpacing()
                    }
                    left.node?.let { leftNode ->
                        if (leftNode.elementType === PARAMETER) {
                            // let us check whether it is a mistakenly parsed parameter such as \begin{equation} [x+y]^2 \end{equation}
                            val envElement = parent.node?.psi as? LatexEnvironment
                            val parameterElement = leftNode.psi as? LatexParameter
                            val semantics = envElement?.let { LatexDefinitionService.resolveEnv(it) }
                            if (semantics != null && parameterElement != null) {
                                if (LatexPsiUtil.getCorrespondingArgument(envElement.beginCommand, parameterElement, semantics.arguments) == null) {
                                    return@customRule Spacing.getReadOnlySpacing()
                                }
                            }
                        }
                    }
                }
                return@customRule null
            }
        }

        simple {
            between(NORMAL_TEXT_WORD, NORMAL_TEXT_WORD).spaces(1)
            before(ENVIRONMENT_CONTENT).lineBreakInCode()
            before(PSEUDOCODE_BLOCK_CONTENT).lineBreakInCode()
            before(IF_BLOCK_CONTENT).lineBreakInCode()
        }

        // Newline before certain algorithm pseudocode commands
        custom {
            customRule { parent, _, right ->
                // Lowercase to also catch \STATE from algorithmic
                newLineBeforeState(latexCommonSettings, parent, right)
            }
        }

        custom {
            // Insert a new line between the end of environment content and the end command.
            inPosition(parent = ENVIRONMENT, left = ENVIRONMENT_CONTENT, right = END_COMMAND).spacing(
                Spacing.createSpacing(0, Int.MAX_VALUE, 1, latexCommonSettings.KEEP_LINE_BREAKS, latexCommonSettings.KEEP_BLANK_LINES_IN_CODE)
            )
            inPosition(parent = PSEUDOCODE_BLOCK, left = PSEUDOCODE_BLOCK_CONTENT, right = END_PSEUDOCODE_BLOCK).spacing(
                Spacing.createSpacing(0, Int.MAX_VALUE, 1, latexCommonSettings.KEEP_LINE_BREAKS, latexCommonSettings.KEEP_BLANK_LINES_IN_CODE)
            )
            inPosition(parent = IF_BLOCK, left = IF_BLOCK_CONTENT, right = END_IF).spacing(
                Spacing.createSpacing(0, Int.MAX_VALUE, 1, latexCommonSettings.KEEP_LINE_BREAKS, latexCommonSettings.KEEP_BLANK_LINES_IN_CODE)
            )
        }

        custom {
            fun commentSpacing(minSpaces: Int): Spacing {
                if (latexCommonSettings.KEEP_FIRST_COLUMN_COMMENT) {
                    return Spacing.createKeepingFirstColumnSpacing(minSpaces, Int.MAX_VALUE, latexCommonSettings.KEEP_LINE_BREAKS, latexCommonSettings.KEEP_BLANK_LINES_IN_CODE)
                }
                return createSpacing(minSpaces, Int.MAX_VALUE, 0, latexCommonSettings.KEEP_LINE_BREAKS, latexCommonSettings.KEEP_BLANK_LINES_IN_CODE)
            }

            inPosition(right = COMMENT_TOKEN).spacing(commentSpacing(0))
        }

        custom {
            // Make sure the number of new lines before a sectioning command is as much as the user specified in the settings.
            // BUG OR FEATURE? Does not work for a command that immediately follows \begin{document}.
            customRule { _, _, right ->
                sectionSpacing(latexSettings, right)
            }
        }

        // Align & in tables.
        // Unfortunately we have to do this manually because Alignment only aligns characters if they are the first
        // non-whitespace in a line of code
        custom {
            customRule { parent, _, right ->
                leftTableSpaceAlign(latexCommonSettings, parent, right)
            }
            customRule { parent, left, _ ->
                rightTableSpaceAlign(latexCommonSettings, parent, left)
            }
        }
    }
}

fun sectionSpacing(
    latexSettings: LatexCodeStyleSettings,
    right: ASTBlock
): Spacing? {
    // Because getting the full text from a node is relatively expensive, we first use a condition which is necessary (but not sufficient) as an early exit.
    // psi should be a no_math_content containing the sectioning command.
    val psi = right.node?.psi as? LatexNoMathContent ?: return null
    val commandPSI = psi.commands ?: return null
    val commandName = commandPSI.name ?: return null
    val spacingVar = LatexCodeStyleSettings.blankLinesOptions[commandName] ?: return null
    if (psi.parentOfType(LatexCommands::class)?.name in CommandMagic.definitions || commandPSI.requiredParameters().isEmpty()) {
        // if it is contained in a definition or has no parameters, we should not treat it as a sectioning command
        return null
    }
    return createSpacing(
        minSpaces = 0,
        maxSpaces = Int.MAX_VALUE,
        minLineFeeds = spacingVar.get(latexSettings) + 1,
        keepLineBreaks = false,
        keepBlankLines = 0
    )
}

fun newLineBeforeState(latexCommonSettings: CommonCodeStyleSettings, parent: ASTBlock, right: ASTBlock): Spacing? {
    val parentPsi = parent.node?.psi ?: return null
    if (!parentPsi.inDirectEnvironment(EnvironmentMagic.algorithmEnvironments)) {
        return null
    }
    val rightPsi = right.node?.psi ?: return null
    val commandName = rightPsi.asCommandName()?.lowercase() ?: return null
    if (commandName != "\\state" && commandName != "\\statex") {
        return null
    }
    return Spacing.createSpacing(0, 1, 1, latexCommonSettings.KEEP_LINE_BREAKS, latexCommonSettings.KEEP_BLANK_LINES_IN_CODE)
}