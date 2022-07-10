package nl.hannahsten.texifyidea.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

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
    var autoCompileOnSaveOnly = false
    var continuousPreview = false
    var includeBackslashInSelection = false
    var showPackagesInStructureView = false
    var automaticQuoteReplacement = QuoteReplacement.NONE

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
            automaticQuoteReplacement = automaticQuoteReplacement,
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
        automaticQuoteReplacement = state.automaticQuoteReplacement
    }
}
