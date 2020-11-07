package nl.hannahsten.texifyidea.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.util.Magic
import java.awt.Component
import java.awt.FlowLayout
import javax.swing.*

/**
 * @author Hannah Schellekens, Sten Wessel
 */
@Suppress("SameParameterValue")
class TexifyConfigurable : SearchableConfigurable {

    private val settings: TexifySettings = TexifySettings.getInstance()

    private lateinit var automaticSecondInlineMathSymbol: JBCheckBox
    private lateinit var automaticUpDownBracket: JBCheckBox
    private lateinit var automaticItemInItemize: JBCheckBox
    private lateinit var automaticDependencyCheck: JBCheckBox
    private lateinit var autoCompile: JBCheckBox
    private lateinit var continuousPreview: JBCheckBox
    private lateinit var includeBackslashInSelection: JBCheckBox
    private lateinit var showPackagesInStructureView: JBCheckBox
    private lateinit var automaticQuoteReplacement: ComboBox<String>
    private lateinit var missingLabelMinimumLevel: ComboBox<LatexRegularCommand>

    override fun getId() = "TexifyConfigurable"

    override fun getDisplayName() = "TeXiFy"

    override fun createComponent(): JComponent? {
        return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(
                JPanel().apply {
                    layout = BoxLayout(this, BoxLayout.Y_AXIS)

                    automaticSecondInlineMathSymbol = addCheckbox("Automatically insert second '$'")
                    automaticUpDownBracket = addCheckbox("Automatically insert braces around text in subscript and superscript")
                    automaticItemInItemize = addCheckbox("Automatically insert '\\item' in itemize-like environments on pressing enter")
                    automaticDependencyCheck = addCheckbox("Automatically check for required package dependencies and insert them")
                    autoCompile = addCheckbox("Automatic compilation (warning: can cause high CPU usage)")
                    continuousPreview = addCheckbox("Automatically refresh preview of math and TikZ pictures")
                    includeBackslashInSelection = addCheckbox("Include the backslash in the selection when selecting a LaTeX command")
                    showPackagesInStructureView = addCheckbox("Show LaTeX package files in structure view (warning: structure view will take more time to load)")
                    automaticQuoteReplacement = addSmartQuotesOptions("Off", "TeX ligatures", "TeX commands", "csquotes")
                    missingLabelMinimumLevel = addMissingLabelMinimumLevel()
                    addPdfViewerText()
                }
            )
        }
    }

    /**
     * Add the options for the smart quote substitution.
     */
    private fun JPanel.addSmartQuotesOptions(vararg values: String): ComboBox<String> {
        val list = ComboBox(values)
        add(
            JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(JBLabel("Smart quote substitution: "))
                add(list)
            }
        )
        return list
    }

    private fun JPanel.addMissingLabelMinimumLevel(): ComboBox<LatexRegularCommand> {
        val list = ComboBox(Magic.Command.labeledLevels.keys.toTypedArray())
        add(
            JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(JBLabel("Minimum sectioning level which should trigger the missing label inspection: "))
                add(list)
            }
        )
        list.renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, celHasFocus: Boolean): Component {
                val item = (value as? LatexRegularCommand)?.command ?: value
                return super.getListCellRendererComponent(list, item, index, isSelected, celHasFocus)
            }
        }
        return list
    }

    private fun JPanel.addPdfViewerText() {
        val oldPdfViewer = TexifySettings.getInstance().pdfViewer
        add(
            JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(JLabel("<html>Note: This setting has been moved to the run configuration (template). See the wiki for details.<br/>Old PDF viewer: $oldPdfViewer</html>"))
                add(JLabel("Old PDF viewer: $oldPdfViewer"))
            }
        )
    }

    private fun JPanel.addCheckbox(message: String): JBCheckBox {
        val checkBox = JBCheckBox(message)
        add(
            JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(checkBox)
            }
        )
        return checkBox
    }

    override fun isModified(): Boolean {
        return automaticSecondInlineMathSymbol.isSelected != settings.automaticSecondInlineMathSymbol ||
            automaticUpDownBracket.isSelected != settings.automaticUpDownBracket ||
            automaticItemInItemize.isSelected != settings.automaticItemInItemize ||
            automaticDependencyCheck.isSelected != settings.automaticDependencyCheck ||
            autoCompile.isSelected != settings.autoCompile ||
            continuousPreview.isSelected != settings.continuousPreview ||
            includeBackslashInSelection.isSelected != settings.includeBackslashInSelection ||
            showPackagesInStructureView.isSelected != settings.showPackagesInStructureView ||
            automaticQuoteReplacement.selectedIndex != settings.automaticQuoteReplacement.ordinal ||
            missingLabelMinimumLevel.selectedItem != settings.missingLabelMinimumLevel
    }

    override fun apply() {
        settings.automaticSecondInlineMathSymbol = automaticSecondInlineMathSymbol.isSelected
        settings.automaticUpDownBracket = automaticUpDownBracket.isSelected
        settings.automaticItemInItemize = automaticItemInItemize.isSelected
        settings.automaticDependencyCheck = automaticDependencyCheck.isSelected
        settings.autoCompile = autoCompile.isSelected
        settings.continuousPreview = continuousPreview.isSelected
        settings.includeBackslashInSelection = includeBackslashInSelection.isSelected
        settings.showPackagesInStructureView = showPackagesInStructureView.isSelected
        settings.automaticQuoteReplacement = TexifySettings.QuoteReplacement.values()[automaticQuoteReplacement.selectedIndex]
        settings.missingLabelMinimumLevel = missingLabelMinimumLevel.selectedItem as LatexRegularCommand
    }

    override fun reset() {
        automaticSecondInlineMathSymbol.isSelected = settings.automaticSecondInlineMathSymbol
        automaticUpDownBracket.isSelected = settings.automaticUpDownBracket
        automaticItemInItemize.isSelected = settings.automaticItemInItemize
        automaticDependencyCheck.isSelected = settings.automaticDependencyCheck
        autoCompile.isSelected = settings.autoCompile
        continuousPreview.isSelected = settings.continuousPreview
        includeBackslashInSelection.isSelected = settings.includeBackslashInSelection
        showPackagesInStructureView.isSelected = settings.showPackagesInStructureView
        automaticQuoteReplacement.selectedIndex = settings.automaticQuoteReplacement.ordinal
        missingLabelMinimumLevel.selectedItem = settings.missingLabelMinimumLevel
    }
}
