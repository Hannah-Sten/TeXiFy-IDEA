package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsScheme.Companion.PROJECT_SCHEME_NAME

data class TexifyConventionsProjectState(
    var scheme: TexifyConventionsScheme = TexifyConventionsScheme(
        myName = PROJECT_SCHEME_NAME,
    ),
    var useProjectScheme: Boolean = false
) {

    fun deepCopy() = copy(scheme = scheme.deepCopy())
}

@State(name = "Conventions", storages = [Storage("texifySettings.xml")])
class TexifyConventionsProjectSettingsManager(var project: Project? = null) :
    PersistentStateComponent<TexifyConventionsProjectState> {

    private var projectState: TexifyConventionsProjectState = TexifyConventionsProjectState()

    override fun getState(): TexifyConventionsProjectState = projectState

    override fun loadState(newState: TexifyConventionsProjectState) {
        if (!newState.scheme.isProjectScheme)
            throw IllegalStateException("ProjectSettingsManager can only save project schemes")
        projectState = newState
    }
}