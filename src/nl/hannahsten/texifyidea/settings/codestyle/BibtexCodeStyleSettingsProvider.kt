package nl.hannahsten.texifyidea.settings.codestyle

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.psi.codeStyle.CodeStyleConfigurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import nl.hannahsten.texifyidea.BibtexLanguage

class BibtexCodeStyleSettingsProvider : CodeStyleSettingsProvider() {

    override fun createCustomSettings(settings: CodeStyleSettings) = BibtexCodeStyleSettings(settings)

    override fun getConfigurableDisplayName() = BibtexLanguage.displayName

    override fun createConfigurable(settings: CodeStyleSettings, originalSettings: CodeStyleSettings): CodeStyleConfigurable {
        return object : CodeStyleAbstractConfigurable(settings, originalSettings, configurableDisplayName) {

            override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
                val language = BibtexLanguage

                return object : TabbedLanguageCodeStylePanel(language, currentSettings, settings) {

                    override fun initTabs(settings: CodeStyleSettings) {
                        addIndentOptionsTab(settings)
                        addWrappingAndBracesTab(settings)
                    }

                    override fun addWrappingAndBracesTab(settings: CodeStyleSettings?) {
                        addTab(object : MyWrappingAndBracesPanel(settings) {
                            // Remove "Braces" from tab title
                            override fun getTabTitle() = "Wrapping"
                        })
                    }
                }
            }
        }
    }
}