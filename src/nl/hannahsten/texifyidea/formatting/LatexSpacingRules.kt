package nl.hannahsten.texifyidea.formatting

import com.intellij.formatting.Spacing
import com.intellij.psi.codeStyle.CodeStyleSettings
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.formatting.spacingrules.leftTableSpaceAlign
import nl.hannahsten.texifyidea.formatting.spacingrules.rightTableSpaceAlign
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexTypes.*
import nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettings
import nl.hannahsten.texifyidea.util.firstChildOfType
import nl.hannahsten.texifyidea.util.inDirectEnvironment
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.parentOfType
import java.util.*

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
            customRule { parent, _, right ->
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
                }
                return@customRule null
            }
        }

        simple {
            between(NORMAL_TEXT_WORD, NORMAL_TEXT_WORD).spaces(1)
            before(ENVIRONMENT_CONTENT).lineBreakInCode()
            before(PSEUDOCODE_BLOCK_CONTENT).lineBreakInCode()
        }

        // Newline before certain algorithm pseudocode commands
        custom {
            customRule { parent, _, right ->
                // Lowercase to also catch \STATE from algorithmic
                if (right.node?.psi?.firstChildOfType(LatexCommands::class)?.name?.lowercase(Locale.getDefault()) in setOf(
                        "\\state",
                        "\\statex"
                    ) && parent.node?.psi?.inDirectEnvironment(EnvironmentMagic.algorithmEnvironments) == true
                ) {
                    return@customRule Spacing.createSpacing(0, 1, 1, latexCommonSettings.KEEP_LINE_BREAKS, latexCommonSettings.KEEP_BLANK_LINES_IN_CODE)
                }

                return@customRule null
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
                // Because getting the full text from a node is relatively expensive, we first use a condition which is necessary (but not sufficient) as an early exit.
                val commandName = right.node?.psi?.firstChildOfType(LatexCommands::class)?.name
                if (commandName !in LatexCodeStyleSettings.blankLinesOptions.values) return@customRule null

                val rightText = right.node?.text
                LatexCodeStyleSettings.blankLinesOptions.forEach {
                    if (rightText?.matches(Regex("\\\\${it.value.trimStart('\\')}\\{.*}")) == true &&
                        !CommandMagic.definitions.contains(right.node?.psi?.parentOfType(LatexCommands::class)?.name)
                    ) {
                        return@customRule createSpacing(
                            minSpaces = 0,
                            maxSpaces = Int.MAX_VALUE,
                            minLineFeeds = it.key.get(latexSettings) + 1,
                            keepLineBreaks = false,
                            keepBlankLines = 0
                        )
                    }
                }
                return@customRule null
            }
        }

        // Align & in tables.
        // Unfortunately we have to do this manually because Alignment only aligns characters if they are the first
        // non-whitespace in a line of code
        custom {
            customRule { parent, _, right ->
                return@customRule leftTableSpaceAlign(latexCommonSettings, parent, right)
            }
            customRule { parent, left, _ ->
                return@customRule rightTableSpaceAlign(latexCommonSettings, parent, left)
            }
        }
    }
}