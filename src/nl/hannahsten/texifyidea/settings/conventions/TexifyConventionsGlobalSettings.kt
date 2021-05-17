package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import nl.hannahsten.texifyidea.settings.TexifyConventionsGlobalScheme
import nl.hannahsten.texifyidea.settings.TexifyConventionsScheme

@State(name = "Conventions", storages = [Storage("texifySettings.xml")])
data class TexifyConventionsGlobalSettings(
    var currentSchemeName: String = TexifyConventionsScheme.DEFAULT_SCHEME_NAME,
    var schemes: List<TexifyConventionsGlobalScheme> = listOf(TexifyConventionsGlobalScheme())
) : PersistentStateComponent<TexifyConventionsGlobalSettings> {

    fun deepCopy() = copy(schemes = schemes.map { it.copy() }.toMutableList())

    override fun getState(): TexifyConventionsGlobalSettings = this

    override fun loadState(state: TexifyConventionsGlobalSettings) = XmlSerializerUtil.copyBean(state, this)

}