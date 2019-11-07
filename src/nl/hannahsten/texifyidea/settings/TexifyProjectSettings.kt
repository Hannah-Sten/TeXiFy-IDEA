package nl.hannahsten.texifyidea.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler

/**
 * Project-level settings are stored in the workspace file.
 * For more information, see https://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html
 */
@State(name = "TexifyProjectSettings", storages = [(Storage(StoragePathMacros.WORKSPACE_FILE))])
data class TexifyProjectSettings(
        var compilerCompatibility: LatexCompiler = LatexCompiler.PDFLATEX
) : PersistentStateComponent<TexifyProjectSettings> {

    companion object {
        @JvmStatic
        fun getInstance(project: Project): TexifyProjectSettings = ServiceManager.getService(project, TexifyProjectSettings::class.java)
    }

    override fun getState() = this

    override fun loadState(state: TexifyProjectSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}