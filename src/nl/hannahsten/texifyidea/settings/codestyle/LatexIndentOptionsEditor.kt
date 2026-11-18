package nl.hannahsten.texifyidea.settings.codestyle

import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import nl.hannahsten.texifyidea.TexifyBundle
import java.awt.event.ItemEvent
import javax.swing.JCheckBox

/**
 * Customizing the 'Tabs and Indents' settings cannot be done via [LatexLanguageCodeStyleSettingsProvider.customizeSettings] using INDENT_SETTINGS but has to be done here.
 *
 * See e.g. the YAML plugin "YAMLLanguageCodeStyleSettingsProvider"
 */
class LatexIndentOptionsEditor(provider: LatexLanguageCodeStyleSettingsProvider) : SmartIndentOptionsEditor(provider) {

    private var sectionIndents: JCheckBox? = null
    private var environmentIndent: JCheckBox? = null
    private var documentIndent: JCheckBox? = null

    override fun addComponents() {
        super.addComponents()

        val section = JCheckBox(TexifyBundle.message("settings.codestyle.indent.nested.sections")).also { sectionIndents = it }
        val env = JCheckBox(TexifyBundle.message("settings.codestyle.indent.environments")).also { environmentIndent = it }
        val doc = JCheckBox(TexifyBundle.message("settings.codestyle.indent.document.environment")).also { documentIndent = it }

        // (De)select the document environment checkbox whenever the environment checkbox is (de)selected.
        env.addItemListener { event -> doc.isSelected = event.stateChange == ItemEvent.SELECTED }

        add(section)
        add(env)
        add(doc, true)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        sectionIndents?.isEnabled = enabled
        environmentIndent?.isEnabled = enabled
        documentIndent?.isEnabled = enabled
    }

    override fun isModified(settings: CodeStyleSettings?, options: CommonCodeStyleSettings.IndentOptions?): Boolean {
        val isModified = super.isModified(settings, options)
        val latexSettings = settings?.getCustomSettings(LatexCodeStyleSettings::class.java) ?: return false

        val section = sectionIndents ?: return isModified
        val env = environmentIndent ?: return isModified
        val doc = documentIndent ?: return isModified

        return isModified ||
            isFieldModified(section, latexSettings.INDENT_SECTIONS) ||
            isFieldModified(env, latexSettings.INDENT_ENVIRONMENTS) ||
            isFieldModified(doc, latexSettings.INDENT_DOCUMENT_ENVIRONMENT)
    }

    override fun apply(settings: CodeStyleSettings?, options: CommonCodeStyleSettings.IndentOptions?) {
        super.apply(settings, options)

        val latexSettings = settings?.getCustomSettings(LatexCodeStyleSettings::class.java) ?: return
        sectionIndents?.let { latexSettings.INDENT_SECTIONS = it.isSelected }
        environmentIndent?.let { latexSettings.INDENT_ENVIRONMENTS = it.isSelected }
        documentIndent?.let { latexSettings.INDENT_DOCUMENT_ENVIRONMENT = it.isSelected }
    }

    override fun reset(settings: CodeStyleSettings, options: CommonCodeStyleSettings.IndentOptions) {
        super.reset(settings, options)

        val latexSettings = settings.getCustomSettings(LatexCodeStyleSettings::class.java)
        sectionIndents?.isSelected = latexSettings.INDENT_SECTIONS
        environmentIndent?.isSelected = latexSettings.INDENT_ENVIRONMENTS
        documentIndent?.isSelected = latexSettings.INDENT_DOCUMENT_ENVIRONMENT
    }
}
