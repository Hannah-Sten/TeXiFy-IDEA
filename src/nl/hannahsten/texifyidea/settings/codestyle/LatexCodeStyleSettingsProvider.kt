package nl.hannahsten.texifyidea.settings.codestyle

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.psi.codeStyle.CodeStyleConfigurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import nl.hannahsten.texifyidea.LatexLanguage

/**
 * Provides the LaTeX code style settings.
 *
 * The most useful thing here is that we specify which tabs to show in the
 * settings.
 *
 * @author Sten Wessel
 */
class LatexCodeStyleSettingsProvider : CodeStyleSettingsProvider() {

    override fun createCustomSettings(settings: CodeStyleSettings) = LatexCodeStyleSettings(settings)

    override fun getConfigurableDisplayName() = LatexLanguage.displayName

    override fun createConfigurable(settings: CodeStyleSettings, originalSettings: CodeStyleSettings): CodeStyleConfigurable {
        return object : CodeStyleAbstractConfigurable(settings, originalSettings, configurableDisplayName) {

            override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
                val language = LatexLanguage

                return object : TabbedLanguageCodeStylePanel(language, currentSettings, settings) {

                    override fun initTabs(settings: CodeStyleSettings) {
                        addIndentOptionsTab(settings)
                        addWrappingAndBracesTab(settings)
                        addBlankLinesTab(settings)

                        // Adds the Code Generation tab.
                        for (provider in EXTENSION_POINT_NAME.extensions) {
                            if (provider.language === LatexLanguage && !provider.hasSettingsPage()) {
                                createTab(provider)
                            }
                        }
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