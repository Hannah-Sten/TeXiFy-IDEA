package nl.hannahsten.texifyidea.settings.codestyle

import com.intellij.application.options.codeStyle.CommenterForm
import com.intellij.openapi.application.ApplicationBundle
import com.intellij.psi.codeStyle.CodeStyleConfigurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import com.intellij.psi.codeStyle.DisplayPriority
import com.intellij.ui.IdeBorderFactory
import com.intellij.util.ui.JBInsets
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Copied from the Kotlin plugin.
 *
 * @author Abby Berkers
 */
class LatexGenerationSettingsProvider : CodeStyleSettingsProvider() {

    override fun createConfigurable(settings: CodeStyleSettings, modelSettings: CodeStyleSettings): CodeStyleConfigurable {
        return LatexCodeStyleGenerationConfigurable(settings)
    }

    override fun getConfigurableDisplayName(): String = ApplicationBundle.message("title.code.generation")
    override fun getPriority(): DisplayPriority = DisplayPriority.CODE_SETTINGS
    override fun hasSettingsPage() = false
    override fun getLanguage(): LatexLanguage = LatexLanguage
}

class LatexCodeStyleGenerationConfigurable(private val mySettings: CodeStyleSettings) : CodeStyleConfigurable {

    private val myCommenterForm: CommenterForm = CommenterForm(LatexLanguage)

    override fun getDisplayName(): String = ApplicationBundle.message("title.code.generation")

    override fun createComponent(): JComponent {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = IdeBorderFactory.createEmptyBorder(JBInsets(0, 10, 10, 10))
            add(myCommenterForm.commenterPanel)
        }
    }

    override fun isModified(): Boolean {
        return myCommenterForm.isModified(mySettings)
    }

    override fun apply() {
        apply(mySettings)
    }

    override fun reset() {
        reset(mySettings)
    }

    override fun reset(settings: CodeStyleSettings) {
        myCommenterForm.reset(settings)
    }

    override fun apply(settings: CodeStyleSettings) {
        myCommenterForm.apply(settings)
    }
}