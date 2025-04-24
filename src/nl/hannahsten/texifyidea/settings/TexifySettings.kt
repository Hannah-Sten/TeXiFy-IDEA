package nl.hannahsten.texifyidea.settings

import com.intellij.ide.PowerSaveMode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import nl.hannahsten.texifyidea.run.linuxpdfviewer.InternalPdfViewer

/**
 * @author Sten Wessel
 */
@State(name = "TexifySettings", storages = [(Storage("texifySettings.xml", roamingType = RoamingType.DEFAULT))])
class TexifySettings : PersistentStateComponent<TexifySettingsState> {

    companion object {

        /**
         * Warning: don't retrieve the settings on class initialization (e.g. storing it in a companion object), as that is not unlikely to throw a ProcessCanceledException.
         */
        @JvmStatic
        fun getInstance(): TexifySettings = ApplicationManager.getApplication().getService(TexifySettings::class.java)
    }

    /** Options for smart quote replacement, in the order as they appear in the combobox **/
    enum class QuoteReplacement {

        NONE,
        LIGATURES,
        COMMANDS,
        CSQUOTES // Context Sensitive quotes from the csquotes package
    }

    /** Paste provider configuration, similar to Editor > General > Smart Keys **/
    enum class HtmlPasteTranslator {
        BUILTIN,
        PANDOC,
        DISABLED,
    }

    enum class AutoCompile {
        OFF,
        ALWAYS,
        AFTER_DOCUMENT_SAVE,
        DISABLE_ON_POWER_SAVE,
    }

    var automaticSecondInlineMathSymbol = true
    var automaticUpDownBracket = true
    var automaticItemInItemize = true
    var automaticDependencyCheck = true
    var automaticBibtexImport = true
    var continuousPreview = false
    var includeBackslashInSelection = false
    var showPackagesInStructureView = false
    var enableExternalIndex = true
    var enableSpellcheckEverywhere = false
    var enableTextidote = false
    var textidoteOptions = "--check en --output singleline --no-color"
    var latexIndentOptions = ""
    var automaticQuoteReplacement = QuoteReplacement.NONE
    var htmlPasteTranslator = HtmlPasteTranslator.BUILTIN
    var autoCompileOption = AutoCompile.OFF

    /**
     * Backwards compatibility. This value is never altered, only read from/to memory.
     *
     * We keep it here so that when the user migrates from when the pdf viewer was set in TeXiFy settings to when it is
     * set in the run config, we can recover their old setting.
     */
    var pdfViewer: InternalPdfViewer? = null

    override fun getState(): TexifySettingsState {
        return TexifySettingsState(
            automaticSecondInlineMathSymbol = automaticSecondInlineMathSymbol,
            automaticUpDownBracket = automaticUpDownBracket,
            automaticItemInItemize = automaticItemInItemize,
            automaticDependencyCheck = automaticDependencyCheck,
            automaticBibtexImport = automaticBibtexImport,
            continuousPreview = continuousPreview,
            includeBackslashInSelection = includeBackslashInSelection,
            showPackagesInStructureView = showPackagesInStructureView,
            enableExternalIndex = enableExternalIndex,
            enableSpellcheckEverywhere = enableSpellcheckEverywhere,
            enableTextidote = enableTextidote,
            textidoteOptions = textidoteOptions,
            latexIndentOptions = latexIndentOptions,
            automaticQuoteReplacement = automaticQuoteReplacement,
            htmlPasteTranslator = htmlPasteTranslator,
            autoCompileOption = autoCompileOption,
            pdfViewer = pdfViewer
        )
    }

    override fun loadState(state: TexifySettingsState) {
        automaticSecondInlineMathSymbol = state.automaticSecondInlineMathSymbol
        automaticUpDownBracket = state.automaticUpDownBracket
        automaticItemInItemize = state.automaticItemInItemize
        automaticDependencyCheck = state.automaticDependencyCheck
        automaticBibtexImport = state.automaticBibtexImport
        continuousPreview = state.continuousPreview
        includeBackslashInSelection = state.includeBackslashInSelection
        showPackagesInStructureView = state.showPackagesInStructureView
        enableExternalIndex = state.enableExternalIndex
        enableSpellcheckEverywhere = state.enableSpellcheckEverywhere
        enableTextidote = state.enableTextidote
        textidoteOptions = state.textidoteOptions
        latexIndentOptions = state.latexIndentOptions
        automaticQuoteReplacement = state.automaticQuoteReplacement
        htmlPasteTranslator = state.htmlPasteTranslator
        // Backwards compatibility
        autoCompileOption = state.autoCompileOption ?: if (state.autoCompileOnSaveOnly) AutoCompile.AFTER_DOCUMENT_SAVE else if (state.autoCompile) AutoCompile.ALWAYS else AutoCompile.OFF
        pdfViewer = state.pdfViewer
    }

    fun isAutoCompileEnabled(): Boolean {
        return autoCompileOption == AutoCompile.ALWAYS || (!PowerSaveMode.isEnabled() && autoCompileOption == AutoCompile.DISABLE_ON_POWER_SAVE)
    }
}
