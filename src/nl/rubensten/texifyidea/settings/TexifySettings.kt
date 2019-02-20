package nl.rubensten.texifyidea.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import java.awt.Label

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

    var automaticSoftWraps = false
    var automaticSecondInlineMathSymbol = true
    var automaticUpDownBracket = true
    var automaticItemInItemize = true

    private var _labelCommands: HashMap<String, LabelingCommandInformation> =
            hashMapOf("\\label" to LabelingCommandInformation("\\label", 1, true))
    val labelCommands: Map<String, LabelingCommandInformation>
    get() {
        return _labelCommands.mapKeys { it.key.addLeadingSlash() }
    }

    override fun getState() = this

    override fun loadState(state: TexifySettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    @Deprecated(message = "Deprecated because of using more Kotlin like getters and setters",
            replaceWith = ReplaceWith("labelCommands"))
    fun getLabelCommandsLeadingSlash() = labelCommands

    fun addCommand(cmd: LabelingCommandInformation) {
        _labelCommands[cmd.commandName] = cmd
    }

    fun removeCommand(cmdName: String) {
        _labelCommands.remove(cmdName)
    }

    private fun String.addLeadingSlash(): String {
        return if (this[0] == '\\') this else "\\" + this
    }
}
