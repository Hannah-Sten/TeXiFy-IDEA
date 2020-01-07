package nl.hannahsten.texifyidea.formatting

import com.intellij.formatting.Spacing
import com.intellij.psi.codeStyle.CodeStyleSettings
import nl.hannahsten.texifyidea.BibtexLanguage
import nl.hannahsten.texifyidea.psi.BibtexTypes.*
import nl.hannahsten.texifyidea.settings.codestyle.BibtexCodeStyleSettings

fun createBibtexSpacingBuilder(settings: CodeStyleSettings): TexSpacingBuilder {
    fun createSpacing(minSpaces: Int, maxSpaces: Int, minLineFeeds: Int, keepLineBreaks: Boolean, keepBlankLines: Int): Spacing =
            Spacing.createSpacing(minSpaces, maxSpaces, minLineFeeds, keepLineBreaks, keepBlankLines)

    val bibtexSettings = settings.getCustomSettings(BibtexCodeStyleSettings::class.java)
    val bibtexCommonSettings = settings.getCommonSettings(BibtexLanguage)

    return rules(bibtexCommonSettings) {

        simple {
            around(ASSIGNMENT).spaces(1)
            before(SEPARATOR).spaces(0)
            between(TYPE, OPEN_BRACE).spaces(0)
            between(TYPE, OPEN_PARENTHESIS).spaces(0)
            between(OPEN_BRACE, ID).spaces(0)
            after(OPEN_PARENTHESIS).spaces(1)
            around(CONCATENATE).spaces(1)
            between(ENTRY_CONTENT, ENDTRY).spaces(1)
        }

        custom {
            // Only insert a space between two actual words, so when the left word
            // is not a left brace, and the right word is not a right brace.
            customRule { _, left, right ->
                return@customRule if (right.node?.elementType === NORMAL_TEXT_WORD && left.node?.elementType === NORMAL_TEXT_WORD) {
                    if (left.node?.text == "{" || right.node?.text == "}") null
                    else Spacing.createSpacing(1, 1, 0, bibtexCommonSettings.KEEP_LINE_BREAKS, bibtexCommonSettings.KEEP_BLANK_LINES_IN_CODE)
                }
                else null
            }
        }
    }
}