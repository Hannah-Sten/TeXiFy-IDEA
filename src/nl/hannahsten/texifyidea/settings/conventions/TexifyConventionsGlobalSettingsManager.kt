package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

data class TexifyConventionsGlobalState(
    var selectedScheme: String = TexifyConventionsScheme.DEFAULT_SCHEME_NAME,
    var schemes: List<TexifyConventionsScheme> = listOf(TexifyConventionsScheme())
) {
    fun deepCopy() = copy(schemes = schemes.map { it.copy() })
}

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