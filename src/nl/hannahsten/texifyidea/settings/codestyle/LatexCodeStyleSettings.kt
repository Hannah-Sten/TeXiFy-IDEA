package nl.hannahsten.texifyidea.settings.codestyle

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.util.magic.cmd

/**
 * Defines all the custom code style settings.
 *
 * @author Abby Berkers
 */
@Suppress("PropertyName")
class LatexCodeStyleSettings(container: CodeStyleSettings) : CustomCodeStyleSettings(LatexLanguage.INSTANCE.id, container) {

    /**
     * The number of blank lines to use before a sectioning command.
     */
    // JvmField is required to avoid NoSuchFieldException when referring to the field name in LatexLanguageCodeStyleSettingsProvider
    @JvmField var BLANK_LINES_BEFORE_PART: Int = 2
    @JvmField var BLANK_LINES_BEFORE_CHAPTER: Int = 2
    @JvmField var BLANK_LINES_BEFORE_SECTION: Int = 2
    @JvmField var BLANK_LINES_BEFORE_SUBSECTION: Int = 1
    @JvmField var BLANK_LINES_BEFORE_SUBSUBSECTION: Int = 1
    @JvmField var BLANK_LINES_BEFORE_PARAGRAPH: Int = 1
    @JvmField var BLANK_LINES_BEFORE_SUBPARAGRAPH: Int = 1

    /**
     * Indent text inside sections.
     */
    @JvmField var INDENT_SECTIONS = true

    companion object {

        val blankLinesOptions = mapOf(
            LatexCodeStyleSettings::BLANK_LINES_BEFORE_PART to LatexGenericRegularCommand.PART.cmd,
            LatexCodeStyleSettings::BLANK_LINES_BEFORE_CHAPTER to LatexGenericRegularCommand.CHAPTER.cmd,
            LatexCodeStyleSettings::BLANK_LINES_BEFORE_SECTION to LatexGenericRegularCommand.SECTION.cmd,
            LatexCodeStyleSettings::BLANK_LINES_BEFORE_SUBSECTION to LatexGenericRegularCommand.SUBSECTION.cmd,
            LatexCodeStyleSettings::BLANK_LINES_BEFORE_SUBSUBSECTION to LatexGenericRegularCommand.SUBSUBSECTION.cmd,
            LatexCodeStyleSettings::BLANK_LINES_BEFORE_PARAGRAPH to LatexGenericRegularCommand.PARAGRAPH.cmd,
            LatexCodeStyleSettings::BLANK_LINES_BEFORE_SUBPARAGRAPH to LatexGenericRegularCommand.SUBPARAGRAPH.cmd
        )
    }
}