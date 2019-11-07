package nl.hannahsten.texifyidea.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.xmlb.XmlSerializerUtil
import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer

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
        COMMANDS,
        CSQUOTES // Context Sensitive quotes from the csquotes package
    }

    var automaticSoftWraps = false
    var automaticSecondInlineMathSymbol = true
    var automaticUpDownBracket = true
    var automaticItemInItemize = true

    // Index of selected combobox item
    var automaticQuoteReplacement = QuoteReplacement.NONE
    var pdfViewer = PdfViewer.values().first { it.isAvailable() }

    override fun getState() = this

    override fun loadState(state: TexifySettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
