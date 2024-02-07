package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

/**
 * Settings manager that persists and loads settings from a project local settings file (i.e., below the .idea folder).
 *
 * The class is internal because clients should use the [TexifyConventionsSettingsManager] facade.
 */
@State(name = "Conventions", storages = [Storage("texifySettings.xml", roamingType = RoamingType.DEFAULT)])
@Service(Service.Level.PROJECT)
internal class TexifyConventionsProjectSettingsManager(var project: Project? = null) :
    PersistentStateComponent<TexifyConventionsProjectState> {

    private var projectState: TexifyConventionsProjectState = TexifyConventionsProjectState()

    override fun getState(): TexifyConventionsProjectState = projectState

    override fun loadState(newState: TexifyConventionsProjectState) {
        if (!newState.scheme.isProjectScheme)
            throw IllegalStateException("ProjectSettingsManager can only save project schemes")
        projectState = newState
    }
}