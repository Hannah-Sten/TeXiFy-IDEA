package nl.rubensten.texifyidea.settings

import com.intellij.openapi.options.SearchableConfigurable
import java.awt.FlowLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

/**
 *
 * @author Sten Wessel
 */
class TexifyConfigurable(private val settings: TexifySettings) : SearchableConfigurable {

    private lateinit var automaticSoftWraps: JCheckBox

    override fun getId() = "TexifyConfigurable"

    override fun getDisplayName() = "TeXiFy"

    override fun createComponent(): JComponent {
        return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            automaticSoftWraps = JCheckBox("Enable soft wraps when opening LaTeX files")
            add(automaticSoftWraps)
        }
    }

    override fun isModified(): Boolean {
        return automaticSoftWraps.isSelected != settings.automaticSoftWraps
    }

    override fun apply() {
        settings.automaticSoftWraps = automaticSoftWraps.isSelected
    }

    override fun reset() {
        automaticSoftWraps.isSelected = settings.automaticSoftWraps
    }
}
