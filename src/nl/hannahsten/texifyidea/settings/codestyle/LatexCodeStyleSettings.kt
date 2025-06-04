package nl.hannahsten.texifyidea.settings.codestyle

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.util.magic.cmd

/**
 * Defines all the custom code style settings.
 *
 * @author Abby Berkers
 */
@Suppress("PropertyName")
class LatexCodeStyleSettings(container: CodeStyleSettings) : CustomCodeStyleSettings(LatexLanguage.id, container) {

    /**
     * The number of blank lines to use before a sectioning command.
     */
    // JvmField is required to avoid NoSuchFieldException when referring to the field name in LatexLanguageCodeStyleSettingsProvider
    @JvmField
    var BLANK_LINES_BEFORE_PART: Int = 2

    @JvmField
    var BLANK_LINES_BEFORE_CHAPTER: Int = 2

    @JvmField
    var BLANK_LINES_BEFORE_SECTION: Int = 2

    @JvmField
    var BLANK_LINES_BEFORE_SUBSECTION: Int = 1

    @JvmField
    var BLANK_LINES_BEFORE_SUBSUBSECTION: Int = 1

    @JvmField
    var BLANK_LINES_BEFORE_PARAGRAPH: Int = 1

    @JvmField
    var BLANK_LINES_BEFORE_SUBPARAGRAPH: Int = 1

    /**
     * Indent text inside sections.
     */
    @JvmField
    var INDENT_SECTIONS = false

    /**
     * Indent environments.
     */
    @JvmField
    var INDENT_ENVIRONMENTS = true

    /**
     * Indent the document environment, this overrides the [INDENT_ENVIRONMENTS] setting for the document environment.
     */
    @JvmField
    var INDENT_DOCUMENT_ENVIRONMENT = true

    companion object {

        val blankLinesOptions = mapOf(
            LatexGenericRegularCommand.PART.cmd to LatexCodeStyleSettings::BLANK_LINES_BEFORE_PART,
            LatexGenericRegularCommand.CHAPTER.cmd to LatexCodeStyleSettings::BLANK_LINES_BEFORE_CHAPTER,
            LatexGenericRegularCommand.SECTION.cmd to LatexCodeStyleSettings::BLANK_LINES_BEFORE_SECTION,
            LatexGenericRegularCommand.SUBSECTION.cmd to LatexCodeStyleSettings::BLANK_LINES_BEFORE_SUBSECTION,
            LatexGenericRegularCommand.SUBSUBSECTION.cmd to LatexCodeStyleSettings::BLANK_LINES_BEFORE_SUBSUBSECTION,
            LatexGenericRegularCommand.PARAGRAPH.cmd to LatexCodeStyleSettings::BLANK_LINES_BEFORE_PARAGRAPH,
            LatexGenericRegularCommand.SUBPARAGRAPH.cmd to LatexCodeStyleSettings::BLANK_LINES_BEFORE_SUBPARAGRAPH
        )

    }
}