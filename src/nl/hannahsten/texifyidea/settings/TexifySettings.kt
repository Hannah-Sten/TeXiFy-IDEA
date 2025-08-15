package nl.hannahsten.texifyidea.settings

import com.intellij.ide.PowerSaveMode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.run.pdfviewer.SumatraViewer

@Service
@State(name = "TexifySettings", storages = [(Storage("texifySettings.xml", roamingType = RoamingType.DEFAULT))])
class TexifySettings : SimplePersistentStateComponent<TexifySettings.State>(State()) {

    companion object {
        /**
         * Default expiration time for filesets in milliseconds.
         */
        const val DEFAULT_FILESET_EXPIRATION_TIME_MS = 2000 // Default expiration time for filesets in milliseconds, 2 seconds

        const val DEFAULT_TEXTIDOTE_OPTIONS = "--check en --output singleline --no-color"

        /**
         * Warning: don't retrieve the settings on class initialization (e.g. storing it in a companion object), as that is not unlikely to throw a ProcessCanceledException.
         */
        @JvmStatic
        fun getInstance(): TexifySettings {
            return ApplicationManager.getApplication().service()
        }

        @JvmStatic
        fun getState() = getInstance().state
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

    enum class CompletionMode {
        /**
         * Show all completions entries from all packages, not context-aware.
         */
        ALL_PACKAGES,

        /**
         * Show only completions entries from the current file and its dependencies, but not context-aware.
         */
        BASIC,

        /**
         * Show only context-aware completions entries from the current file and its dependencies.
         */
        CONTEXT_AWARE,
    }

    class State : BaseState() {
        var automaticSecondInlineMathSymbol by property(true)
        var automaticUpDownBracket by property(true)
        var automaticItemInItemize by property(true)
        var automaticDependencyCheck by property(true)
        var automaticBibtexImport by property(true)
        var continuousPreview by property(false)
        var includeBackslashInSelection by property(false)
        var showPackagesInStructureView by property(false)
        var enableExternalIndex by property(true)
        var enableSpellcheckEverywhere by property(false)
        var enableTextidote by property(false)
        var textidoteOptions by string(DEFAULT_TEXTIDOTE_OPTIONS)
        var latexIndentOptions by string("")
        var automaticQuoteReplacement by enum(QuoteReplacement.NONE)
        var htmlPasteTranslator by enum(HtmlPasteTranslator.BUILTIN)
        var autoCompileOption by enum<AutoCompile>(AutoCompile.OFF)
        var missingLabelMinimumLevel by enum(LatexGenericRegularCommand.SUBSECTION)
        var pathToSumatra by string(null)
        var hasApprovedDetexify by property(false)
        var filesetExpirationTimeMs by property(DEFAULT_FILESET_EXPIRATION_TIME_MS)
    }

    override fun initializeComponent() {
        state.pathToSumatra?.let {
            SumatraViewer.trySumatraPath(it)
        }
    }

    fun isAutoCompileEnabled(): Boolean {
        return when (state.autoCompileOption) {
            AutoCompile.OFF -> false
            AutoCompile.ALWAYS, AutoCompile.AFTER_DOCUMENT_SAVE -> true
            AutoCompile.DISABLE_ON_POWER_SAVE -> !PowerSaveMode.isEnabled()
        }
    }

    /**
     * Returns true if the auto compile should be triggered immediately after a change in the document.
     */
    fun isAutoCompileImmediate(): Boolean {
        return when (state.autoCompileOption) {
            AutoCompile.ALWAYS -> true
            AutoCompile.OFF, AutoCompile.AFTER_DOCUMENT_SAVE -> false
            AutoCompile.DISABLE_ON_POWER_SAVE -> !PowerSaveMode.isEnabled()
        }
    }
}
