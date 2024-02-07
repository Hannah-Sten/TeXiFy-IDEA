package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Settings manager that persists and loads settings from a global settings file.
 *
 * The class is internal because clients should use the [TexifyConventionsSettingsManager] facade.
 * This class cannot be a light service, because it is application-level but needs to have the same roaming type as other components using texifySettings.xml, see
 * https://plugins.jetbrains.com/docs/intellij/plugin-services.html#light-services
 * https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html#defining-the-storage-location
 */
@State(name = "Conventions", storages = [Storage("texifySettings.xml", roamingType = RoamingType.DEFAULT)])
internal class TexifyConventionsGlobalSettingsManager : PersistentStateComponent<TexifyConventionsGlobalState> {

    private var globalState: TexifyConventionsGlobalState = TexifyConventionsGlobalState()

    override fun getState(): TexifyConventionsGlobalState = globalState

    override fun loadState(newState: TexifyConventionsGlobalState) {
        if (newState.schemes.any { it.isProjectScheme })
            throw IllegalStateException("GlobalSettingsManager cannot save project schemes")
        globalState = newState
    }
}