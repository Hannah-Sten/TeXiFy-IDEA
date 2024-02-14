package nl.hannahsten.texifyidea.settings.codestyle

import com.intellij.application.options.IndentOptionsEditor
import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import java.awt.event.ItemEvent
import javax.swing.JCheckBox

/**
 * Customizing the 'Tabs and Indents' settings cannot be done via [LatexLanguageCodeStyleSettingsProvider.customizeSettings] using INDENT_SETTINGS but has to be done here.
 *
 * See e.g. the YAML plugin "YAMLLanguageCodeStyleSettingsProvider"
 */
class LatexIndentOptionsEditor(provider: LatexLanguageCodeStyleSettingsProvider) : SmartIndentOptionsEditor(provider) {

    private val sectionIndents = JCheckBox("Nested indent of sections")
    private val environmentIndent = JCheckBox("Indent environments")
    private val documentIndent = JCheckBox("Indent document environment")

    init {
        // (De)select the document environment checkbox whenever the environment checkbox is (de)selected.
        environmentIndent.addItemListener { event -> documentIndent.isSelected = event.stateChange == ItemEvent.SELECTED }
    }

    override fun addComponents() {
        super.addComponents()
        add(sectionIndents)
        add(environmentIndent)
        add(documentIndent, true)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        sectionIndents.isEnabled = enabled
        environmentIndent.isEnabled = enabled
        documentIndent.isEnabled = enabled
    }

    override fun isModified(settings: CodeStyleSettings?, options: CommonCodeStyleSettings.IndentOptions?): Boolean {
        val isModified = super.isModified(settings, options)
        val latexSettings = settings?.getCustomSettings(LatexCodeStyleSettings::class.java) ?: return false
        return isModified ||
            IndentOptionsEditor.isFieldModified(sectionIndents, latexSettings.INDENT_SECTIONS) ||
            IndentOptionsEditor.isFieldModified(environmentIndent, latexSettings.INDENT_ENVIRONMENTS) ||
            IndentOptionsEditor.isFieldModified(documentIndent, latexSettings.INDENT_DOCUMENT_ENVIRONMENT)
    }

    override fun apply(settings: CodeStyleSettings?, options: CommonCodeStyleSettings.IndentOptions?) {
        super.apply(settings, options)

        val latexSettings = settings?.getCustomSettings(LatexCodeStyleSettings::class.java)
        latexSettings?.INDENT_SECTIONS = sectionIndents.isSelected
        latexSettings?.INDENT_ENVIRONMENTS = environmentIndent.isSelected
        latexSettings?.INDENT_DOCUMENT_ENVIRONMENT = documentIndent.isSelected
    }

    override fun reset(settings: CodeStyleSettings, options: CommonCodeStyleSettings.IndentOptions) {
        super.reset(settings, options)

        val latexSettings = settings.getCustomSettings(LatexCodeStyleSettings::class.java)
        sectionIndents.isSelected = latexSettings.INDENT_SECTIONS
        environmentIndent.isSelected = latexSettings.INDENT_ENVIRONMENTS
        documentIndent.isSelected = latexSettings.INDENT_DOCUMENT_ENVIRONMENT
    }
}