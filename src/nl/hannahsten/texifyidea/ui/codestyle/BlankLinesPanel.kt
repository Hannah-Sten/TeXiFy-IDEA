package nl.hannahsten.texifyidea.ui.codestyle

import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.ui.components.JBScrollPane
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettings
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.removeHtmlTags
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class BlankLinesPanelWrapper(settings: CodeStyleSettings) : CodeStyleAbstractPanel(settings) {
    private val blankLinesPanel = BlankLinesPanel(settings)

    private fun CodeStyleSettings.latexSettings() = getCustomSettings(LatexCodeStyleSettings::class.java)

    override fun getRightMargin() = throw UnsupportedOperationException()

    override fun createHighlighter(scheme: EditorColorsScheme) =
            EditorHighlighterFactory.getInstance().createEditorHighlighter(LatexFileType, scheme, null)

    override fun getFileType() = throw UnsupportedOperationException()

    override fun getPreviewText() = Magic.General.demoText.removeHtmlTags()

    override fun apply(settings: CodeStyleSettings) = blankLinesPanel.apply(settings.latexSettings())

    override fun isModified(settings: CodeStyleSettings): Boolean = blankLinesPanel.isModified(settings.latexSettings())

    override fun getPanel() = blankLinesPanel

    override fun resetImpl(settings: CodeStyleSettings) {
        blankLinesPanel.reset(settings.latexSettings())
    }

    override fun getTabTitle() = "Blank Lines"
}

class BlankLinesPanel(private val settings: CodeStyleSettings) : JPanel() {
    private val sectionBlankLines = JSpinner(SpinnerNumberModel(0, 0, Int.MAX_VALUE, 1))

    init {
        layout = BorderLayout()
        add(JBScrollPane(JPanel(GridBagLayout()).apply {
            val constraints = GridBagConstraints().apply {
                weightx = 1.0
                insets = Insets(0, 10, 10, 10)
                fill = GridBagConstraints.HORIZONTAL
                gridy = 0
            }

            add(sectionBlankLines, constraints.apply { gridy++ })
        }), BorderLayout.CENTER)
    }

    fun reset(settings: LatexCodeStyleSettings) {
        sectionBlankLines.value = settings.BLANK_LINES_BEFORE_SECTION
    }

    fun apply(settings: LatexCodeStyleSettings) {
        settings.BLANK_LINES_BEFORE_SECTION = sectionBlankLines.value as Int
    }

    fun isModified(settings: LatexCodeStyleSettings): Boolean {
        return with(settings) {
            var isModified: Boolean = false
            isModified = isModified || settings.BLANK_LINES_BEFORE_SECTION != sectionBlankLines.value

            isModified
        }
    }
}