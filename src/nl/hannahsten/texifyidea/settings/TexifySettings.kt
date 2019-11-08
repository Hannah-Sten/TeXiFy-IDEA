package nl.hannahsten.texifyidea.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.xmlb.XmlSerializerUtil
import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer

/**
 * @author Sten Wessel
 */
@State(name = "TexifySettings", storages = [(Storage("texifySettings.xml"))])
class TexifySettings : PersistentStateComponent<TexifySettingsState> {

    companion object {
        @JvmStatic
        fun getInstance(): TexifySettings = ServiceManager.getService(TexifySettings::class.java)
    }

    // Options for smart quote replacement, in the order as they appear in the combobox
    enum class QuoteReplacement {
        NONE,
        LIGATURES,
        COMMANDS,
        CSQUOTES // Context Sensitive quotes from the csquotes package
    }

    var automaticSoftWraps = false
    var automaticSecondInlineMathSymbol = true
    var automaticUpDownBracket = true
    var automaticItemInItemize = true
    var continuousPreview = false
    var automaticQuoteReplacement = QuoteReplacement.NONE
    var pdfViewer = PdfViewer.values().first { it.isAvailable() }

    /**
     * internal list which stores the commands data
     */
    val labelCommands: HashMap<String, LabelingCommandInformation> =
            hashMapOf("\\label" to LabelingCommandInformation("\\label", 1, true))

    override fun getState(): TexifySettingsState? {
        return TexifySettingsState(
                labelCommands = labelCommands.mapValues { it.value.toSerializableString() },
                automaticSoftWraps = automaticSoftWraps,
                automaticSecondInlineMathSymbol = automaticSecondInlineMathSymbol,
                automaticUpDownBracket = automaticUpDownBracket,
                automaticItemInItemize = automaticItemInItemize,
                automaticQuoteReplacement = automaticQuoteReplacement
        )
    }

    override fun loadState(state: TexifySettingsState) {
        state.labelCommands.forEach { labelCommands[it.key] = LabelingCommandInformation.fromString(it.value) }
        automaticSoftWraps = state.automaticSoftWraps
        automaticSecondInlineMathSymbol = state.automaticSecondInlineMathSymbol
        automaticUpDownBracket = state.automaticUpDownBracket
        automaticItemInItemize = state.automaticItemInItemize
        automaticQuoteReplacement = state.automaticQuoteReplacement
    }

    fun addCommand(cmd: LabelingCommandInformation) {
        labelCommands[cmd.commandName] = cmd
    }

    fun removeCommand(cmdName: String) {
        labelCommands.remove(cmdName)
    }

    /**
     * all commands in this map could be used to label a previous command like 'section'
     */
    val labelPreviousCommands: Map<String, LabelingCommandInformation>
        get() = labelCommands.filter { it.value.labelsPreviousCommand }
}
