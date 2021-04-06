package nl.hannahsten.texifyidea.settings.codestyle

import com.intellij.application.options.IndentOptionsEditor
import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import javax.swing.JCheckBox

/**
 * Customizing the 'Tabs and Indents' settings cannot be done via [LatexLanguageCodeStyleSettingsProvider.customizeSettings] using INDENT_SETTINGS but has to be done here.
 *
 * todo should be unselected by default
 *
 * See e.g. the YAML plugin "YAMLLanguageCodeStyleSettingsProvider"
 */
class LatexIndentOptionsEditor(provider: LatexLanguageCodeStyleSettingsProvider) : SmartIndentOptionsEditor(provider) {

    private val sectionIndents = JCheckBox("Nested indent of sections")

    override fun addComponents() {
        super.addComponents()
        add(sectionIndents)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        sectionIndents.isEnabled = enabled
    }

    override fun isModified(settings: CodeStyleSettings?, options: CommonCodeStyleSettings.IndentOptions?): Boolean {
        val isModified = super.isModified(settings, options)
        val latexSettings = settings?.getCustomSettings(LatexCodeStyleSettings::class.java) ?: return false
        return isModified || IndentOptionsEditor.isFieldModified(sectionIndents, latexSettings.INDENT_SECTIONS)
    }

    override fun apply(settings: CodeStyleSettings?, options: CommonCodeStyleSettings.IndentOptions?) {
        super.apply(settings, options)

        val latexSettings = settings?.getCustomSettings(LatexCodeStyleSettings::class.java)
        latexSettings?.INDENT_SECTIONS = sectionIndents.isSelected
    }

    override fun reset(settings: CodeStyleSettings, options: CommonCodeStyleSettings.IndentOptions) {
        super.reset(settings, options)

        val latexSettings = settings.getCustomSettings(LatexCodeStyleSettings::class.java)
        sectionIndents.isSelected = latexSettings.INDENT_SECTIONS
    }
}