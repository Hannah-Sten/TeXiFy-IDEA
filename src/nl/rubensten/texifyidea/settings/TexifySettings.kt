package nl.rubensten.texifyidea.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 *
 * @author Sten Wessel
 */
@State(name = "TexifySettings", storages = [(Storage("texifySettings.xml"))])
class TexifySettings : PersistentStateComponent<TexifySettings> {

    companion object {

        @JvmStatic
        fun getInstance(): TexifySettings = ServiceManager.getService(TexifySettings::class.java)
    }

    // Options for smart quote replacement, in the order as they appear in the combobox
    enum class QuoteReplacement {
        NONE,
        LIGATURES,
        COMMANDS
    }

    var automaticSoftWraps = false
    var automaticSecondInlineMathSymbol = true
    var automaticUpDownBracket = true
    var automaticItemInItemize = true

    // Index of selected combobox item
    var automaticQuoteReplacement = QuoteReplacement.NONE

    /**
     * internal list which stores the commands data
     */
    private val labelCommandsInternal: HashMap<String, LabelingCommandInformation> =
            hashMapOf("\\label" to LabelingCommandInformation("\\label", 1, true))

    /**
     * Verify that any key in the list has a leading backslash
     */
    val labelCommands: Map<String, LabelingCommandInformation>
        get() {
            return labelCommandsInternal.mapKeys { it.key.addLeadingSlash() }
        }

    /**
     * Do not use this field anywhere! It is only needed to persist the data of the commands to the settings.
     * It maps any command to its string representation and on first load back to the object
     */
    var labelCommandsAsString: Map<String, String>
        get() {
            return labelCommandsInternal.mapValues { it.value.toSerializableString() }
        }
        set(value) {
            value.forEach { (key, info) -> labelCommandsInternal[key] = LabelingCommandInformation.fromString(info) }
        }

    override fun getState() = this

    override fun loadState(state: TexifySettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun addCommand(cmd: LabelingCommandInformation) {
        labelCommandsInternal[cmd.commandName] = cmd
    }

    fun removeCommand(cmdName: String) {
        labelCommandsInternal.remove(cmdName)
    }

    val labelAnyCommands: Map<String, LabelingCommandInformation>
        get() = labelCommands.filter { it.value.labelsPreviousCommand }

    private fun String.addLeadingSlash(): String {
        return if (this[0] == '\\') this else "\\" + this
    }
}
