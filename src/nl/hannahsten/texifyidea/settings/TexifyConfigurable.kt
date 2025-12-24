package nl.hannahsten.texifyidea.settings

import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.fields.IntegerField
import nl.hannahsten.texifyidea.run.pdfviewer.SumatraViewer
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.enums.EnumEntries
import kotlin.reflect.KMutableProperty0

/**
 * @author Hannah Schellekens, Sten Wessel
 */
@Suppress("SameParameterValue")
class TexifyConfigurable : SearchableConfigurable {

    private val settings = TexifySettings.getState()

    private var automaticSecondInlineMathSymbol: JBCheckBox? = null
    private var automaticUpDownBracket: JBCheckBox? = null
    private var automaticItemInItemize: JBCheckBox? = null
    private var automaticDependencyCheck: JBCheckBox? = null
    private var automaticBibtexImport: JBCheckBox? = null
    private var continuousPreview: JBCheckBox? = null
    private var includeBackslashInSelection: JBCheckBox? = null
    private var showPackagesInStructureView: JBCheckBox? = null
    private var enableExternalIndex: JBCheckBox? = null
    private var enableSpellcheckEverywhere: JBCheckBox? = null
    private var enableTextidote: JBCheckBox? = null
    private var textidoteOptions: RawCommandLineEditor? = null
    private var latexIndentOptions: RawCommandLineEditor? = null
    private var automaticQuoteReplacement: ComboBox<String>? = null
    private var htmlPasteTranslator: ComboBox<String>? = null
    private var autoCompileOption: ComboBox<String>? = null
    private var sumatraPath: TextFieldWithBrowseButton? = null
    private var filesetExpirationTimeMs: IntegerField? = null
    private var completionMode: ComboBox<String>? = null

    /**
     * Helper class to map settings to UI components.
     */
    private data class EnumSetting<E : Enum<E>>(
        val comboBox: KMutableProperty0<ComboBox<String>?>,
        val setting: KMutableProperty0<E>,
        val values: EnumEntries<E>
    ) {
        fun setValueBySelected() {
            val selectedIndex = comboBox.get()?.selectedIndex ?: 0
            setting.set(values[selectedIndex])
        }
    }

    /**
     * Map UI variables to underlying setting variables
     */
    private val booleanSettings = settings.let { state ->
        listOf(
            Pair(::automaticSecondInlineMathSymbol, state::automaticSecondInlineMathSymbol),
            Pair(::automaticUpDownBracket, state::automaticUpDownBracket),
            Pair(::automaticItemInItemize, state::automaticItemInItemize),
            Pair(::automaticDependencyCheck, state::automaticDependencyCheck),
            Pair(::automaticBibtexImport, state::automaticBibtexImport),
            Pair(::continuousPreview, state::continuousPreview),
            Pair(::includeBackslashInSelection, state::includeBackslashInSelection),
            Pair(::showPackagesInStructureView, state::showPackagesInStructureView),
            Pair(::enableExternalIndex, state::enableExternalIndex),
            Pair(::enableSpellcheckEverywhere, state::enableSpellcheckEverywhere),
            Pair(::enableTextidote, state::enableTextidote),
        )
    }

    /**
     * Map UI variables to underlying setting variables
     */
    private val enumSettings = settings.let { state ->
        listOf(
            EnumSetting(::completionMode, state::completionMode, TexifySettings.CompletionMode.entries),
            EnumSetting(::automaticQuoteReplacement, state::automaticQuoteReplacement, TexifySettings.QuoteReplacement.entries),
            EnumSetting(::htmlPasteTranslator, state::htmlPasteTranslator, TexifySettings.HtmlPasteTranslator.entries),
            EnumSetting(::autoCompileOption, state::autoCompileOption, TexifySettings.AutoCompile.entries),
        )
    }

    override fun getId() = "TexifyConfigurable"

    override fun getDisplayName() = "TeXiFy"

    override fun createComponent(): JComponent = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
        add(
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                automaticSecondInlineMathSymbol = addCheckbox("Automatically insert second '$'")
                automaticUpDownBracket = addCheckbox("Automatically insert braces around text in subscript and superscript")
                automaticItemInItemize = addCheckbox("Automatically insert '\\item' in itemize-like environments on pressing enter")
                completionMode = addComboBox("Autocompletion mode", "Smart", "Included only", "All packages")
                automaticDependencyCheck = addCheckbox("Automatically check for required package dependencies and insert them")
                automaticBibtexImport = addCheckbox("Automatically copy BibTeX entries from remote libraries to the local library")
                continuousPreview = addCheckbox("Automatically refresh preview of math and TikZ pictures")
                includeBackslashInSelection = addCheckbox("Include the backslash in the selection when selecting a LaTeX command")
                showPackagesInStructureView = addCheckbox("Show LaTeX package files in structure view (warning: structure view will take more time to load)")
                enableExternalIndex = addCheckbox("Enable indexing of MiKTeX/TeX Live package files (requires restart)")
                enableSpellcheckEverywhere = addCheckbox("Enable spellcheck inspection in all scopes")
                enableTextidote = addCheckbox("Enable the Textidote linter")
                textidoteOptions = addCommandLineEditor("Textidote", TexifySettings.DEFAULT_TEXTIDOTE_OPTIONS)
                latexIndentOptions = addCommandLineEditor("Latexindent", "")
                addSumatraPathField(this)
                automaticQuoteReplacement = addComboBox("Smart quote substitution: ", "Off", "TeX ligatures", "TeX commands", "csquotes")
                htmlPasteTranslator = addComboBox("HTML paste translator", "Built-in", "Pandoc", "Disabled")
                autoCompileOption = addComboBox("Automatic compilation", "Off", "Always", "After document save", "Disable in power save mode")
                addFilesetExpirationTimeMs(this)
            }
        )
    }

    /**
     * Add the options for the smart quote substitution.
     */
    private fun JPanel.addComboBox(title: String, vararg values: String): ComboBox<String> {
        val list = ComboBox(values)
        add(
            JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(JBLabel(title))
                add(list)
            }
        )
        return list
    }

    /**
     * Add a field for command options
     *
     * @param label Name of the command
     * @param initialValue Only used to guess a good field width
     */
    private fun JPanel.addCommandLineEditor(label: String, initialValue: String): RawCommandLineEditor {
        val cmdEditor = RawCommandLineEditor()

        // It's magic
        val width = cmdEditor.getFontMetrics(cmdEditor.font).stringWidth(initialValue) + 170
        cmdEditor.minimumSize = Dimension(width, cmdEditor.preferredSize.height)
        cmdEditor.size = Dimension(width, cmdEditor.preferredSize.height)
        cmdEditor.preferredSize = Dimension(width, cmdEditor.preferredSize.height)

        add(
            JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(JBLabel("$label command line options: "))
                add(cmdEditor)
            }
        )
        return cmdEditor
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

    private fun addSumatraPathField(panel: JPanel) {
        if (!SystemInfo.isWindows) {
            return
        }
        val subPanel = JPanel(FlowLayout(FlowLayout.LEFT))

        val enableSumatraPath = JBLabel("Path to SumatraPDF (optional):")
        subPanel.add(enableSumatraPath)

        val sumatraPath = TextFieldWithBrowseButton()
        this.sumatraPath = sumatraPath

        /*
        Choose SumatraPDF executable file.
         */
        sumatraPath.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor().withTitle("SumatraPDF Location")
                    .withDescription("Select the location of the SumatraPDF executable file.")
                    .withFileFilter { it.name == "SumatraPDF.exe" || it.name == "SumatraPDF" } // Allow both .exe and no extension on Windows
            )
        )
        sumatraPath.isEnabled = true
        subPanel.add(sumatraPath)
        // set the path field to stretch to the right
        sumatraPath.preferredSize = Dimension(400, sumatraPath.preferredSize.height)
        panel.add(subPanel)
    }

    private fun getUISumatraPath(): String? {
        val res = sumatraPath?.text?.takeIf { it.isNotBlank() }
        return res
    }

    private fun addFilesetExpirationTimeMs(panel: JPanel) {
        val subPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val label = JBLabel("Fileset refresh period (ms):")
        subPanel.add(label)
        val tips = "The time after which the fileset (formed by \\input, \\usepackage, etc.) is considered expired and will be rebuilt. \n " +
            "A lower value means quicker response for input files, but also a bit more CPU usage. "

        label.toolTipText = tips
        filesetExpirationTimeMs = IntegerField("Fileset expiration time (ms)", 0, Int.MAX_VALUE).apply {
            defaultValue = TexifySettings.DEFAULT_FILESET_EXPIRATION_TIME_MS
            value = settings.filesetExpirationTimeMs
            preferredSize = Dimension(150, preferredSize.height)
            toolTipText = tips
            subPanel.add(this)
        }
        panel.add(subPanel)
    }

    override fun isModified(): Boolean = booleanSettings.any { it.first.get()?.isSelected != it.second.get() } ||
        textidoteOptions?.text != (settings.textidoteOptions ?: "") ||
        latexIndentOptions?.text != (settings.latexIndentOptions ?: "") ||
        enumSettings.any { it.comboBox.get()?.selectedIndex != it.setting.get().ordinal } ||
        getUISumatraPath() != settings.pathToSumatra ||
        filesetExpirationTimeMs?.value != settings.filesetExpirationTimeMs

    override fun apply() {
        for (setting in booleanSettings) {
            setting.second.set(setting.first.get()?.isSelected == true)
        }
        val ss = settings
        ss.textidoteOptions = textidoteOptions?.text
        ss.latexIndentOptions = latexIndentOptions?.text
        for (setting in enumSettings) {
            setting.setValueBySelected()
        }
        val path = getUISumatraPath()
        if (path != null) {
            if (!SumatraViewer.trySumatraPath(path)) {
                throw RuntimeConfigurationError("Path to SumatraPDF is not valid: $path")
            }
        }
        ss.pathToSumatra = path
        ss.filesetExpirationTimeMs = filesetExpirationTimeMs?.value ?: 2000
    }

    override fun reset() {
        for (setting in booleanSettings) {
            setting.first.get()?.isSelected = setting.second.get()
        }
        val state = settings
        textidoteOptions?.text = state.textidoteOptions
        latexIndentOptions?.text = state.latexIndentOptions
        for (setting in enumSettings) {
            setting.comboBox.get()?.selectedIndex = setting.setting.get().ordinal
        }
        sumatraPath?.text = state.pathToSumatra ?: ""
        filesetExpirationTimeMs?.value = state.filesetExpirationTimeMs
    }
}
