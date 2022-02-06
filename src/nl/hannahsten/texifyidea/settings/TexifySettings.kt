package nl.hannahsten.texifyidea.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.run.linuxpdfviewer.InternalPdfViewer

/**
 * @author Sten Wessel
 */
@State(name = "TexifySettings", storages = [(Storage("texifySettings.xml"))])
class TexifySettings : PersistentStateComponent<TexifySettingsState> {

    companion object {

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
    var continuousPreview = false
    var includeBackslashInSelection = false
    var showPackagesInStructureView = false
    var automaticQuoteReplacement = QuoteReplacement.NONE
    var missingLabelMinimumLevel: LatexCommand = LatexGenericRegularCommand.SUBSECTION

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
            continuousPreview = continuousPreview,
            includeBackslashInSelection = includeBackslashInSelection,
            showPackagesInStructureView = showPackagesInStructureView,
            automaticQuoteReplacement = automaticQuoteReplacement,
            missingLabelMinimumLevel = missingLabelMinimumLevel,
            pdfViewer = pdfViewer
        )
    }

    override fun loadState(state: TexifySettingsState) {
        automaticSecondInlineMathSymbol = state.automaticSecondInlineMathSymbol
        automaticUpDownBracket = state.automaticUpDownBracket
        automaticItemInItemize = state.automaticItemInItemize
        automaticDependencyCheck = state.automaticDependencyCheck
        autoCompile = state.autoCompile
        continuousPreview = state.continuousPreview
        includeBackslashInSelection = state.includeBackslashInSelection
        showPackagesInStructureView = state.showPackagesInStructureView
        automaticQuoteReplacement = state.automaticQuoteReplacement
        missingLabelMinimumLevel = state.missingLabelMinimumLevel
        pdfViewer = state.pdfViewer
    }
}
