package nl.hannahsten.texifyidea.run.latex.logtab.ui

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "LatexErrorTreeViewConfiguration", storages = [(Storage(
    StoragePathMacros.WORKSPACE_FILE
))])
data class LatexErrorTreeViewConfiguration(var showOverfullHBox: Boolean = true) :
    PersistentStateComponent<LatexErrorTreeViewConfiguration> {
    companion object {
        @JvmStatic
        fun getInstance(project: Project): LatexErrorTreeViewConfiguration =
            ServiceManager.getService(
                project,
                LatexErrorTreeViewConfiguration::class.java
            )
    }

    override fun getState(): LatexErrorTreeViewConfiguration? {
        return this
    }

    override fun loadState(state: LatexErrorTreeViewConfiguration) {
        XmlSerializerUtil.copyBean(state, this)
    }
}