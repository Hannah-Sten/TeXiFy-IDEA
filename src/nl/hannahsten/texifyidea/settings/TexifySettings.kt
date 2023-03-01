package nl.hannahsten.texifyidea.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import nl.hannahsten.texifyidea.run.linuxpdfviewer.InternalPdfViewer

/**
 * @author Sten Wessel
 */
@State(name = "TexifySettings", storages = [(Storage("texifySettings.xml"))])
class TexifySettings : PersistentStateComponent<TexifySettingsState> {

    companion object {

        /**
         * Warning: don't retrieve the settings on class initialization (e.g. storing it in a companion object), as that is not unlikely to throw a ProcessCanceledException.
         */
        @JvmStatic
        fun getInstance(): TexifySettings = ApplicationManager.getApplication().getService(TexifySettings::class.java)
    }

    // Options for smart quote replacement, in the order as they appear in the combobox
    enum class QuoteReplacement {

        NONE,
        LIGATURES,
        COMMANDS,
        CSQUOTES // Context Sensitive quotes from the csquotes package
    }

    var automaticSecondInlineMathSymbol = true
    var automaticUpDownBracket = true
    var automaticItemInItemize = true
    var automaticDependencyCheck = true
    var autoCompile = false
    var autoCompileOnSaveOnly = false
    var continuousPreview = false
    var includeBackslashInSelection = false
    var showPackagesInStructureView = false
    var enableExternalIndex = true
    var enableTextidote = false
    var textidoteOptions = "--check en --output singleline --no-color"
    var automaticQuoteReplacement = QuoteReplacement.NONE

    /**
     * Backwards compatibility. This value is never altered, only read from/to memory.
     *
     * We keep it here so that when the user migrates from when the pdf viewer was set in TeXiFy settings to when it is
     * set in the run config, we can recover their old setting.
     */
    var pdfViewer = InternalPdfViewer.firstAvailable()

    override fun getState(): TexifySettingsState {
        return TexifySettingsState(
            automaticSecondInlineMathSymbol = automaticSecondInlineMathSymbol,
            automaticUpDownBracket = automaticUpDownBracket,
            automaticItemInItemize = automaticItemInItemize,
            automaticDependencyCheck = automaticDependencyCheck,
            autoCompile = autoCompile,
            autoCompileOnSaveOnly = autoCompileOnSaveOnly,
            continuousPreview = continuousPreview,
            includeBackslashInSelection = includeBackslashInSelection,
            showPackagesInStructureView = showPackagesInStructureView,
            enableExternalIndex = enableExternalIndex,
            enableTextidote = enableTextidote,
            textidoteOptions = textidoteOptions,
            automaticQuoteReplacement = automaticQuoteReplacement,
            pdfViewer = pdfViewer
        )
    }

    override fun loadState(state: TexifySettingsState) {
        automaticSecondInlineMathSymbol = state.automaticSecondInlineMathSymbol
        automaticUpDownBracket = state.automaticUpDownBracket
        automaticItemInItemize = state.automaticItemInItemize
        automaticDependencyCheck = state.automaticDependencyCheck
        autoCompile = state.autoCompile
        autoCompileOnSaveOnly = state.autoCompileOnSaveOnly
        continuousPreview = state.continuousPreview
        includeBackslashInSelection = state.includeBackslashInSelection
        showPackagesInStructureView = state.showPackagesInStructureView
        enableExternalIndex = state.enableExternalIndex
        enableTextidote = state.enableTextidote
        textidoteOptions = state.textidoteOptions
        automaticQuoteReplacement = state.automaticQuoteReplacement
        pdfViewer = state.pdfViewer
    }
}
