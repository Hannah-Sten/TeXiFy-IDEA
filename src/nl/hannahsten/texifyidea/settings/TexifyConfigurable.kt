package nl.hannahsten.texifyidea.settings

import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.showOkCancelDialog
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.fields.IntegerField
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.run.pdfviewer.SumatraViewer
import nl.hannahsten.texifyidea.util.files.LatexIgnoredFileMasks
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JButton
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
    private var addIgnoredLatexMasksButton: JButton? = null
    private var textidoteOptions: RawCommandLineEditor? = null
    private var latexIndentOptions: RawCommandLineEditor? = null
    private var bibtexTidyOptions: RawCommandLineEditor? = null
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

    override fun getDisplayName() = TexifyBundle.message("settings.texify.displayName")

    override fun createComponent(): JComponent = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
        add(
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                automaticSecondInlineMathSymbol = addCheckbox(TexifyBundle.message("settings.automatic.second.inline.math.symbol"))
                automaticUpDownBracket = addCheckbox(TexifyBundle.message("settings.automatic.up.down.bracket"))
                automaticItemInItemize = addCheckbox(TexifyBundle.message("settings.automatic.item.in.itemize"))
                completionMode = addComboBox(
                    TexifyBundle.message("settings.autocompletion.mode"),
                    TexifyBundle.message("settings.autocompletion.mode.smart"),
                    TexifyBundle.message("settings.autocompletion.mode.included.only"),
                    TexifyBundle.message("settings.autocompletion.mode.all.packages")
                )
                automaticDependencyCheck = addCheckbox(TexifyBundle.message("settings.automatic.dependency.check"))
                automaticBibtexImport = addCheckbox(TexifyBundle.message("settings.automatic.bibtex.import"))
                continuousPreview = addCheckbox(TexifyBundle.message("settings.continuous.preview"))
                includeBackslashInSelection = addCheckbox(TexifyBundle.message("settings.include.backslash.in.selection"))
                showPackagesInStructureView = addCheckbox(TexifyBundle.message("settings.show.packages.in.structure.view"))
                enableExternalIndex = addCheckbox(TexifyBundle.message("settings.enable.external.index"))
                enableSpellcheckEverywhere = addCheckbox(TexifyBundle.message("settings.enable.spellcheck.everywhere"))
                enableTextidote = addCheckbox(TexifyBundle.message("settings.enable.textidote"))
                textidoteOptions = addCommandLineEditor(TexifyBundle.message("settings.command.textidote"), TexifySettings.DEFAULT_TEXTIDOTE_OPTIONS)
                latexIndentOptions = addCommandLineEditor(TexifyBundle.message("settings.command.latexindent"), "")
                bibtexTidyOptions = addCommandLineEditor(TexifyBundle.message("settings.command.bibtex.tidy"), "")
                addSumatraPathField(this)
                automaticQuoteReplacement = addComboBox(
                    TexifyBundle.message("settings.smart.quote.substitution"),
                    TexifyBundle.message("settings.option.off"),
                    TexifyBundle.message("settings.smart.quote.substitution.tex.ligatures"),
                    TexifyBundle.message("settings.smart.quote.substitution.tex.commands"),
                    TexifyBundle.message("settings.smart.quote.substitution.csquotes")
                )
                htmlPasteTranslator = addComboBox(
                    TexifyBundle.message("settings.html.paste.translator"),
                    TexifyBundle.message("settings.html.paste.translator.builtin"),
                    TexifyBundle.message("settings.html.paste.translator.pandoc"),
                    TexifyBundle.message("settings.html.paste.translator.disabled")
                )
                autoCompileOption = addComboBox(
                    TexifyBundle.message("settings.automatic.compilation"),
                    TexifyBundle.message("settings.option.off"),
                    TexifyBundle.message("settings.automatic.compilation.always"),
                    TexifyBundle.message("settings.automatic.compilation.after.save"),
                    TexifyBundle.message("settings.automatic.compilation.disable.power.save")
                )
                addFilesetExpirationTimeMs(this)
                addIgnoredLatexMasksAction()
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
                add(JBLabel(TexifyBundle.message("settings.command.line.options", label)))
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

    private fun JPanel.addIgnoredLatexMasksAction() {
        val button = JButton(TexifyBundle.message("settings.ignored.masks.action.text")).apply {
            toolTipText = TexifyBundle.message("settings.ignored.masks.action.tooltip")
            addActionListener {
                promptAndApplyIgnoredLatexMasks()
            }
        }
        addIgnoredLatexMasksButton = button

        add(
            JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(button)
            }
        )
        updateIgnoredLatexMasksButtonState()
    }

    private fun promptAndApplyIgnoredLatexMasks() {
        val currentMasks = LatexIgnoredFileMasks.getCurrentMasks()
        val missingMasks = LatexIgnoredFileMasks.findMissingMasks(currentMasks)
        if (missingMasks.isEmpty()) {
            Messages.showInfoMessage(
                TexifyBundle.message("settings.ignored.masks.info.already.present"),
                TexifyBundle.message("settings.ignored.masks.dialog.title")
            )
            updateIgnoredLatexMasksButtonState()
            return
        }

        val result = showOkCancelDialog(
            TexifyBundle.message("settings.ignored.masks.dialog.title"),
            buildString {
                appendLine(TexifyBundle.message("settings.ignored.masks.dialog.message"))
                appendLine(TexifyBundle.message("settings.ignored.masks.dialog.note"))
                appendLine()
                missingMasks.forEach { appendLine(it) }
            },
            TexifyBundle.message("settings.ignored.masks.dialog.confirm")
        )

        if (result != Messages.OK) {
            return
        }

        LatexIgnoredFileMasks.applyMasks(LatexIgnoredFileMasks.mergeWithPreset(currentMasks))
        updateIgnoredLatexMasksButtonState()
    }

    private fun updateIgnoredLatexMasksButtonState() {
        addIgnoredLatexMasksButton?.isEnabled = LatexIgnoredFileMasks.findMissingMasks(LatexIgnoredFileMasks.getCurrentMasks()).isNotEmpty()
    }

    private fun addSumatraPathField(panel: JPanel) {
        if (!SystemInfo.isWindows) {
            return
        }
        val subPanel = JPanel(FlowLayout(FlowLayout.LEFT))

        val enableSumatraPath = JBLabel(TexifyBundle.message("settings.path.to.sumatra.optional"))
        subPanel.add(enableSumatraPath)

        val sumatraPath = TextFieldWithBrowseButton()
        this.sumatraPath = sumatraPath

        /*
        Choose SumatraPDF executable file.
         */
        sumatraPath.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor().withTitle(TexifyBundle.message("settings.sumatra.location.title"))
                    .withDescription(TexifyBundle.message("settings.sumatra.location.description"))
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
        val label = JBLabel(TexifyBundle.message("settings.fileset.refresh.period.ms"))
        subPanel.add(label)
        val tips = TexifyBundle.message("settings.fileset.refresh.period.tooltip")

        label.toolTipText = tips
        filesetExpirationTimeMs = IntegerField(TexifyBundle.message("settings.fileset.expiration.time.ms"), 0, Int.MAX_VALUE).apply {
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
        bibtexTidyOptions?.text != (settings.bibtexTidyOptions ?: "") ||
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
        ss.bibtexTidyOptions = bibtexTidyOptions?.text
        for (setting in enumSettings) {
            setting.setValueBySelected()
        }
        val path = getUISumatraPath()
        if (path != null) {
            if (!SumatraViewer.trySumatraPath(path)) {
                throw RuntimeConfigurationError(TexifyBundle.message("settings.sumatra.path.invalid", path))
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
        bibtexTidyOptions?.text = state.bibtexTidyOptions
        for (setting in enumSettings) {
            setting.comboBox.get()?.selectedIndex = setting.setting.get().ordinal
        }
        sumatraPath?.text = state.pathToSumatra ?: ""
        filesetExpirationTimeMs?.value = state.filesetExpirationTimeMs
        updateIgnoredLatexMasksButtonState()
    }
}
