package nl.hannahsten.texifyidea.settings.codestyle

import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable.WrappingOrBraceOption.*
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider.SettingsType.BLANK_LINES_SETTINGS
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider.SettingsType.WRAPPING_AND_BRACES_SETTINGS
import com.intellij.psi.codeStyle.extractor.values.Value.VAR_KIND.RIGHT_MARGIN
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.removeHtmlTags

/**
 * Provides the code style settings
 * @author Sten Wessel
 */
class LatexLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {

    companion object {
        private val demoText = Magic.General.latexDemoText.removeHtmlTags()
    }

    override fun getLanguage() = LatexLanguage.INSTANCE!!

    override fun getCodeSample(settingsType: SettingsType) = demoText

    override fun getIndentOptionsEditor() = SmartIndentOptionsEditor()

    override fun getDefaultCommonSettings() = CommonCodeStyleSettings(language).also { it.initIndentOptions() }

    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        when (settingsType) {
            WRAPPING_AND_BRACES_SETTINGS -> customizeWrappingAndBracesSettings(consumer)
            BLANK_LINES_SETTINGS -> customizeBlankLinesSettings(consumer)
            else -> return
        }
    }

    private fun customizeWrappingAndBracesSettings(consumer: CodeStyleSettingsCustomizable) {
        consumer.showStandardOptions(*arrayOf(
                RIGHT_MARGIN,
                WRAP_ON_TYPING,
                WRAP_LONG_LINES,
                KEEP_FIRST_COLUMN_COMMENT
        ).map { it.toString() }.toTypedArray())
    }

    private fun customizeBlankLinesSettings(consumer: CodeStyleSettingsCustomizable) {
        LatexCodeStyleSettings.blankLinesOptions.forEach {
            consumer.showCustomOption(
                    LatexCodeStyleSettings::class.java,
                    it.key.name,
                    it.value,
                    CodeStyleSettingsCustomizable.BLANK_LINES
            )
        }
    }

}