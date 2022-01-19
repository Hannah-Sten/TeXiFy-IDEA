package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Global convention settings.
 *
 * The global settings store a list of schemes, one of which is the global default scheme. In addition, the global
 * settings remember which scheme is currently selected. Instances of this class must be serializable, since they are
 * persisted by a [TexifyConventionsGlobalSettingsManager].
 */
data class TexifyConventionsGlobalState(
    var selectedScheme: String = TexifyConventionsScheme.DEFAULT_SCHEME_NAME,
    var schemes: List<TexifyConventionsScheme> = listOf(TexifyConventionsScheme())
) {

    fun deepCopy() = copy(schemes = schemes.map { it.copy() })
}

/**
 * Settings manager that persists and loads settings from a global settings file.
 */
@State(name = "Conventions", storages = [Storage("texifySettings.xml")])
class TexifyConventionsGlobalSettingsManager : PersistentStateComponent<TexifyConventionsGlobalState> {

    private var globalState: TexifyConventionsGlobalState = TexifyConventionsGlobalState()

    override fun getState(): TexifyConventionsGlobalState = globalState

    override fun loadState(newState: TexifyConventionsGlobalState) {
        if (newState.schemes.any { it.isProjectScheme })
            throw IllegalStateException("GlobalSettingsManager cannot save project schemes")
        globalState = newState
    }
}