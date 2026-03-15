package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "LatexStepLogUiConfiguration",
    storages = [Storage(StoragePathMacros.WORKSPACE_FILE)],
)
@Service(Service.Level.PROJECT)
internal data class LatexStepLogUiConfiguration(
    var expanded: Boolean = true,
    var showBibtexMessages: Boolean = true,
    var showOverfullUnderfullMessages: Boolean = true,
) : PersistentStateComponent<LatexStepLogUiConfiguration> {

    companion object {

        @JvmStatic
        fun getInstance(project: Project): LatexStepLogUiConfiguration =
            project.getService(LatexStepLogUiConfiguration::class.java)
    }

    override fun getState(): LatexStepLogUiConfiguration = this

    override fun loadState(state: LatexStepLogUiConfiguration) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
