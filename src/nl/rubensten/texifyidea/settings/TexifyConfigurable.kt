package nl.rubensten.texifyidea.settings

import com.intellij.openapi.options.SearchableConfigurable
import nl.rubensten.texifyidea.settings.labeldefiningcommands.TexifyConfigurableLabelCommands
import java.awt.FlowLayout
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Ruben Schellekens, Sten Wessel
 */
class TexifyConfigurable(private val settings: TexifySettings) : SearchableConfigurable {

    private lateinit var automaticSoftWraps: JCheckBox
    private lateinit var automaticSecondInlineMathSymbol: JCheckBox
    private lateinit var automaticUpDownBracket: JCheckBox
    private lateinit var automaticItemInItemize: JCheckBox
    private lateinit var labelDefiningCommands: TexifyConfigurableLabelCommands

    override fun getId() = "TexifyConfigurable"

    override fun getDisplayName() = "TeXiFy"

    override fun createComponent(): JComponent? {
        labelDefiningCommands = TexifyConfigurableLabelCommands(settings)

        return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)

                automaticSoftWraps = addCheckbox("Enable soft wraps when opening LaTeX files")
                automaticSecondInlineMathSymbol = addCheckbox("Automatically insert second '$'")
                automaticUpDownBracket = addCheckbox("Automatically insert braces around text in subscript and superscript")
                automaticItemInItemize = addCheckbox("Automatically insert '\\item' in itemize-like environments on pressing enter")

                add(labelDefiningCommands.getTable())
            })
        }
    }

    private fun JPanel.addCheckbox(message: String): JCheckBox {
        val checkBox = JCheckBox(message)
        add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(checkBox)
        })
        return checkBox
    }

    override fun isModified(): Boolean {
        return automaticSoftWraps.isSelected != settings.automaticSoftWraps
                || automaticSecondInlineMathSymbol.isSelected != settings.automaticSecondInlineMathSymbol
                || automaticUpDownBracket.isSelected != settings.automaticUpDownBracket
                || automaticItemInItemize.isSelected != settings.automaticItemInItemize
                || labelDefiningCommands.isModified()
    }

    override fun apply() {
        settings.automaticSoftWraps = automaticSoftWraps.isSelected
        settings.automaticSecondInlineMathSymbol = automaticSecondInlineMathSymbol.isSelected
        settings.automaticUpDownBracket = automaticUpDownBracket.isSelected
        settings.automaticItemInItemize = automaticItemInItemize.isSelected

        labelDefiningCommands.apply()
    }

    override fun reset() {
        automaticSoftWraps.isSelected = settings.automaticSoftWraps
        automaticSecondInlineMathSymbol.isSelected = settings.automaticSecondInlineMathSymbol
        automaticUpDownBracket.isSelected = settings.automaticUpDownBracket
        automaticItemInItemize.isSelected = settings.automaticItemInItemize
        labelDefiningCommands.reset()
    }
}
