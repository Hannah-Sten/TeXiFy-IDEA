package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

data class TexifyConventionsSettings(
    private var projectState: TexifyConventionsProjectState = TexifyConventionsProjectState(
        TexifyConventionsScheme(myName = TexifyConventionsScheme.PROJECT_SCHEME_NAME)
    ),
    private var globalState: TexifyConventionsGlobalState = TexifyConventionsGlobalState()
) {
    fun copyToDefaultScheme(scheme: TexifyConventionsScheme) {
        globalState.schemes =
            globalState.schemes.map { if (it.name == TexifyConventionsScheme.DEFAULT_SCHEME_NAME) scheme.deepCopy() else it }
    }

    fun copyToProjectScheme(scheme: TexifyConventionsScheme) {
        projectState.scheme = scheme.deepCopy()
    }

    fun copyFrom(newState: TexifyConventionsSettings) {
        projectState = newState.projectState.deepCopy()
        globalState = newState.globalState.deepCopy()
    }

    fun getStateCopy() = Pair(projectState.deepCopy(), globalState.deepCopy())

    var currentScheme: TexifyConventionsScheme
        get() {
            return if (projectState.useProjectScheme) projectState.scheme
            else schemes.firstOrNull { it.name == globalState.selectedScheme }
                ?: throw IllegalStateException("No scheme named ${globalState.selectedScheme} exists")
        }
        set(scheme) {
            if (scheme.isProjectScheme) {
                projectState.scheme = scheme.deepCopy()
                projectState.useProjectScheme = true
            }
            else {
                if (!globalState.schemes.any { it.name == scheme.name }) {
                    throw IllegalArgumentException("Scheme ${scheme.name} is neither a project scheme nor a known global scheme")
                }
                globalState.selectedScheme = scheme.name
                projectState.useProjectScheme = false
            }
        }

    val schemes: List<TexifyConventionsScheme>
        get() = listOfNotNull(*globalState.schemes.toTypedArray(), projectState.scheme)

}

class TexifyConventionsSettingsManager(
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