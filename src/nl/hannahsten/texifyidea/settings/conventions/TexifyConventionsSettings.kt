package nl.hannahsten.texifyidea.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsGlobalSettings
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsProjectSettings

data class TexifyConventionsSettings(
    var projectSettings: TexifyConventionsProjectSettings,
    var globalSettings: TexifyConventionsGlobalSettings
) {

    fun loadState(state: TexifyConventionsSettings) {
        projectSettings.loadState(state.projectSettings.scheme)
        globalSettings.loadState(state.globalSettings)
    }

    fun deepCopy() = TexifyConventionsSettings(
        projectSettings = projectSettings.deepCopy(),
        globalSettings = globalSettings.deepCopy()
    )

    var currentScheme: TexifyConventionsScheme
        get() = schemes.firstOrNull { it.name == globalSettings.currentSchemeName }
            ?: throw IllegalStateException("No scheme named ${globalSettings.currentSchemeName} exists")
        set(scheme) {
            if (scheme.isProjectScheme()) {
                projectSettings.scheme = scheme as TexifyConventionsProjectScheme
            }
            else if (!globalSettings.schemes.any { it.name == scheme.name }) {
                throw IllegalArgumentException("Scheme ${scheme.name} is neither a project scheme nor a known global scheme")
            }

            globalSettings.currentSchemeName = scheme.name
        }

    val schemes: List<TexifyConventionsScheme>
        get() = listOfNotNull(*globalSettings.schemes.toTypedArray(), projectSettings.scheme)

    companion object {

        fun getInstance(project: Project) =
            TexifyConventionsSettings(project.getService(TexifyConventionsProjectSettings::class.java), service())
    }
}