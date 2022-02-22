package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * A facade for the [TexifyConventionsProjectSettingsManager] and the [TexifyConventionsGlobalSettingsManager].
 * The class provides a convenient way for the settings UI, inspections or other Texify classes to query the currently
 * active conventions.
 *
 * This class is responsible for delegating the persistence of project and global parts of a [TexifyConventionsSettings]
 * to the appropriate settings manager. Conversely, the class constructs a new [TexifyConventionsSettings] object from
 * project and global state.
 */
class TexifyConventionsSettingsManager private constructor(
    private var projectSettings: TexifyConventionsProjectSettingsManager,
    private var globalSettings: TexifyConventionsGlobalSettingsManager
) {

    fun getSettings() = TexifyConventionsSettings(projectSettings.state, globalSettings.state)

    fun saveSettings(newState: TexifyConventionsSettings) {
        val state = newState.getStateCopy()
        projectSettings.loadState(state.first)
        globalSettings.loadState(state.second)
    }

    companion object {

        fun getInstance(project: Project) =
            TexifyConventionsSettingsManager(
                project.getService(TexifyConventionsProjectSettingsManager::class.java),
                service()
            )
    }
}