package nl.hannahsten.texifyidea.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer
import nl.hannahsten.texifyidea.settings.labeldefiningcommands.TexifyConfigurableLabelCommands
import java.awt.FlowLayout
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Hannah Schellekens, Sten Wessel
 */
class TexifyConfigurable(private val settings: TexifySettings) : SearchableConfigurable {

    private lateinit var automaticSecondInlineMathSymbol: JBCheckBox
    private lateinit var automaticUpDownBracket: JBCheckBox
    private lateinit var automaticItemInItemize: JBCheckBox
    private lateinit var automaticDependencyCheck: JBCheckBox
    private lateinit var autoCompile: JBCheckBox
    private lateinit var continuousPreview: JBCheckBox
    private lateinit var dockerizedMiktex: JBCheckBox
    private lateinit var includeBackslashInSelection: JBCheckBox
    private lateinit var automaticQuoteReplacement: ComboBox<String>
    private lateinit var pdfViewer: ComboBox<String>
    private lateinit var labelDefiningCommands: TexifyConfigurableLabelCommands

    override fun getId() = "TexifyConfigurable"

    override fun getDisplayName() = "TeXiFy"

    override fun createComponent(): JComponent? {
        labelDefiningCommands = TexifyConfigurableLabelCommands(settings)

        return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)

                automaticSecondInlineMathSymbol = addCheckbox("Automatically insert second '$'")
                automaticUpDownBracket = addCheckbox("Automatically insert braces around text in subscript and superscript")
                automaticItemInItemize = addCheckbox("Automatically insert '\\item' in itemize-like environments on pressing enter")
                automaticDependencyCheck = addCheckbox("Automatically check for required package dependencies and insert them")
                autoCompile = addCheckbox("Automatic compilation (warning: can cause high CPU usage)")
                continuousPreview = addCheckbox("Automatically refresh preview of math and TikZ pictures")
                dockerizedMiktex = addCheckbox("Always use Dockerized MiKTeX")
                includeBackslashInSelection = addCheckbox("Include the backslash in the selection when selecting a LaTeX command")
                automaticQuoteReplacement = addSmartQuotesOptions("Off", "TeX ligatures", "TeX commands", "csquotes")
                pdfViewer = addPdfViewerOptions()
                add(labelDefiningCommands.getTable())
            })
        }
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

    private fun JPanel.addPdfViewerOptions(): ComboBox<String> {
            val availableViewers = PdfViewer.availableSubset().map { it.displayName }.toTypedArray()
            val list = ComboBox(availableViewers)
            add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(JBLabel("PDF viewer: "))
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
        return automaticSecondInlineMathSymbol.isSelected != settings.automaticSecondInlineMathSymbol
                || automaticUpDownBracket.isSelected != settings.automaticUpDownBracket
                || automaticItemInItemize.isSelected != settings.automaticItemInItemize
                || automaticDependencyCheck.isSelected != settings.automaticDependencyCheck
                || autoCompile.isSelected != settings.autoCompile
                || continuousPreview.isSelected != settings.continuousPreview
                || dockerizedMiktex.isSelected != settings.dockerizedMiktex
                || includeBackslashInSelection.isSelected != settings.includeBackslashInSelection
                || automaticQuoteReplacement.selectedIndex != settings.automaticQuoteReplacement.ordinal
                || pdfViewer.selectedIndex != settings.pdfViewer.ordinal
                || labelDefiningCommands.isModified()
    }

    override fun apply() {
        settings.automaticSecondInlineMathSymbol = automaticSecondInlineMathSymbol.isSelected
        settings.automaticUpDownBracket = automaticUpDownBracket.isSelected
        settings.automaticItemInItemize = automaticItemInItemize.isSelected
        settings.automaticDependencyCheck = automaticDependencyCheck.isSelected
        settings.autoCompile = autoCompile.isSelected
        settings.continuousPreview = continuousPreview.isSelected
        settings.dockerizedMiktex = dockerizedMiktex.isSelected
        settings.includeBackslashInSelection = includeBackslashInSelection.isSelected
        settings.automaticQuoteReplacement = TexifySettings.QuoteReplacement.values()[automaticQuoteReplacement.selectedIndex]
        settings.pdfViewer = PdfViewer.availableSubset()[pdfViewer.selectedIndex]
        labelDefiningCommands.apply()
    }

    override fun reset() {
        automaticSecondInlineMathSymbol.isSelected = settings.automaticSecondInlineMathSymbol
        automaticUpDownBracket.isSelected = settings.automaticUpDownBracket
        automaticItemInItemize.isSelected = settings.automaticItemInItemize
        automaticDependencyCheck.isSelected = settings.automaticDependencyCheck
        autoCompile.isSelected = settings.autoCompile
        continuousPreview.isSelected = settings.continuousPreview
        dockerizedMiktex.isSelected = settings.dockerizedMiktex
        includeBackslashInSelection.isSelected = settings.includeBackslashInSelection
        automaticQuoteReplacement.selectedIndex = settings.automaticQuoteReplacement.ordinal
        pdfViewer.selectedIndex = PdfViewer.availableSubset().indexOf(settings.pdfViewer)
        labelDefiningCommands.reset()
    }
}
