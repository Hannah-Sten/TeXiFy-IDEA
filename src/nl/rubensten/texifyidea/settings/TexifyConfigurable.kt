package nl.rubensten.texifyidea.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import nl.rubensten.texifyidea.run.LatexCompiler
import java.awt.FlowLayout
import javax.swing.BoxLayout
import javax.swing.JPanel

/**
 * @author Ruben Schellekens, Sten Wessel
 */
class TexifyConfigurable(private val settings: TexifySettings, private val projectSettings: TexifyProjectSettings) : SearchableConfigurable {

    private lateinit var automaticSoftWraps: JBCheckBox
    private lateinit var automaticSecondInlineMathSymbol: JBCheckBox
    private lateinit var automaticUpDownBracket: JBCheckBox
    private lateinit var automaticItemInItemize: JBCheckBox
    private lateinit var automaticQuoteReplacement: ComboBox<String>
    private lateinit var compilerCompatibility: ComboBox<LatexCompiler>

    override fun getId() = "TexifyConfigurable"

    override fun getDisplayName() = "TeXiFy"

    override fun createComponent() = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
        add(JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            automaticSoftWraps = addCheckbox("Enable soft wraps when opening LaTeX files")
            automaticSecondInlineMathSymbol = addCheckbox("Automatically insert second '$'")
            automaticUpDownBracket = addCheckbox("Automatically insert braces around text in subscript and superscript")
            automaticItemInItemize = addCheckbox("Automatically insert '\\item' in itemize-like environments on pressing enter")
            automaticQuoteReplacement = addSmartQuotesOptions("Off", "TeX ligatures", "TeX commands")
            compilerCompatibility = addCompilerCompatibility()

        })
    }

    /**
     * Add the options for the smart quote substitution.
     */
    private fun JPanel.addSmartQuotesOptions(vararg values: String): ComboBox<String> {
        val list = ComboBox(values)
        add(JPanel(FlowLayout(FlowLayout.LEFT)).apply{
            add(JBLabel("Smart quote substitution: "))
            add(list)
        })
        return list
    }

    /**
     * Add the options for the compiler compatibility.
     */
    private fun JPanel.addCompilerCompatibility(): ComboBox<LatexCompiler> {
        // Show available compilers
        val list = ComboBox(LatexCompiler.values())
        add(JPanel(FlowLayout(FlowLayout.LEFT)).apply{
            add(JBLabel("Check for compatibility with compiler: "))
            add(list)
        })
        return list
    }

    private fun JPanel.addCheckbox(message: String): JBCheckBox {
        val checkBox = JBCheckBox(message)
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
                || automaticQuoteReplacement.selectedIndex != settings.automaticQuoteReplacement.ordinal
                || compilerCompatibility.selectedItem != projectSettings.compilerCompatibility
    }

    override fun apply() {
        settings.automaticSoftWraps = automaticSoftWraps.isSelected
        settings.automaticSecondInlineMathSymbol = automaticSecondInlineMathSymbol.isSelected
        settings.automaticUpDownBracket = automaticUpDownBracket.isSelected
        settings.automaticItemInItemize = automaticItemInItemize.isSelected
        settings.automaticQuoteReplacement = TexifySettings.QuoteReplacement.values()[automaticQuoteReplacement.selectedIndex]
        projectSettings.compilerCompatibility = compilerCompatibility.selectedItem as LatexCompiler
    }

    override fun reset() {
        automaticSoftWraps.isSelected = settings.automaticSoftWraps
        automaticSecondInlineMathSymbol.isSelected = settings.automaticSecondInlineMathSymbol
        automaticUpDownBracket.isSelected = settings.automaticUpDownBracket
        automaticItemInItemize.isSelected = settings.automaticItemInItemize
        automaticQuoteReplacement.selectedIndex = settings.automaticQuoteReplacement.ordinal
        compilerCompatibility.selectedItem = projectSettings.compilerCompatibility
    }
}
