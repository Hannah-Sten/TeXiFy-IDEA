package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.settings.TexifyConventionsProjectScheme

@State(name = "Conventions", storages = [Storage("texifySettings.xml")])
data class TexifyConventionsProjectSettings(var project: Project? = null) :
    PersistentStateComponent<TexifyConventionsProjectScheme> {
    var scheme: TexifyConventionsProjectScheme = TexifyConventionsProjectScheme()

    override fun getState(): TexifyConventionsProjectScheme = scheme

    override fun loadState(state: TexifyConventionsProjectScheme) {
        scheme = state
    }

    fun deepCopy() = TexifyConventionsProjectSettings(project).also { it.scheme = scheme }
}