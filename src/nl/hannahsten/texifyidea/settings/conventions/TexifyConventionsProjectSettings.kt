package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsScheme.Companion.PROJECT_SCHEME_NAME

@State(name = "Conventions", storages = [Storage("texifySettings.xml")])
class TexifyConventionsProjectSettings(var project: Project? = null) :
    PersistentStateComponent<TexifyConventionsScheme> {

    var scheme: TexifyConventionsScheme = TexifyConventionsScheme(myName = PROJECT_SCHEME_NAME, isProjectScheme = true)
        set(value) {
            field = value.copy(myName = PROJECT_SCHEME_NAME, isProjectScheme = true)
        }

    override fun getState(): TexifyConventionsScheme = scheme

    override fun loadState(state: TexifyConventionsScheme) {
        scheme = state
    }

    fun deepCopy() = TexifyConventionsProjectSettings(project).also { it.scheme = scheme.copy() }
}