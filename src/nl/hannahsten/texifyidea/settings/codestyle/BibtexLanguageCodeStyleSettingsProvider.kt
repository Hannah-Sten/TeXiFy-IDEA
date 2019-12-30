package nl.hannahsten.texifyidea.settings.codestyle

import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import com.intellij.psi.codeStyle.extractor.values.Value
import nl.hannahsten.texifyidea.BibtexLanguage
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.removeHtmlTags

class BibtexLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    companion object {
        private val demoText = Magic.General.bibtexDemoText.removeHtmlTags()
    }
    override fun getLanguage(): Language = BibtexLanguage

    override fun getCodeSample(settingsType: SettingsType): String? = demoText

    override fun getIndentOptionsEditor() = SmartIndentOptionsEditor()

    override fun getDefaultCommonSettings() = CommonCodeStyleSettings(language).also { it.initIndentOptions() }

    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        when (settingsType) {
            SettingsType.WRAPPING_AND_BRACES_SETTINGS -> customizeWrappingAndBracesSettings(consumer)
            else -> return
        }
    }

    private fun customizeWrappingAndBracesSettings(consumer: CodeStyleSettingsCustomizable) {
        consumer.showStandardOptions(*arrayOf(
                Value.VAR_KIND.RIGHT_MARGIN,
                CodeStyleSettingsCustomizable.WrappingOrBraceOption.WRAP_ON_TYPING,
                CodeStyleSettingsCustomizable.WrappingOrBraceOption.WRAP_LONG_LINES,
                CodeStyleSettingsCustomizable.WrappingOrBraceOption.KEEP_FIRST_COLUMN_COMMENT
        ).map { it.toString() }.toTypedArray())
    }
}