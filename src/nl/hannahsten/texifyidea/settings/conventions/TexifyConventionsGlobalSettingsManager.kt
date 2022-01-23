package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Settings manager that persists and loads settings from a global settings file.
 */
@State(name = "Conventions", storages = [Storage("texifySettings.xml")])
internal class TexifyConventionsGlobalSettingsManager : PersistentStateComponent<TexifyConventionsGlobalState> {

    private var globalState: TexifyConventionsGlobalState = TexifyConventionsGlobalState()

    override fun getState(): TexifyConventionsGlobalState = globalState

    override fun loadState(newState: TexifyConventionsGlobalState) {
        if (newState.schemes.any { it.isProjectScheme })
            throw IllegalStateException("GlobalSettingsManager cannot save project schemes")
        globalState = newState
    }
}