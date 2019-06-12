package nl.rubensten.texifyidea.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.util.xmlb.XmlSerializerUtil
import nl.rubensten.texifyidea.run.LatexCompiler

/**
 * todo
 */
@Storage(StoragePathMacros.WORKSPACE_FILE)
class TexifyProjectSettings : PersistentStateComponent<TexifyProjectSettings> {

    companion object {
        @JvmStatic
        fun getInstance(): TexifyProjectSettings = ServiceManager.getService(TexifyProjectSettings::class.java)
    }

    var compilerCompatibility = LatexCompiler.PDFLATEX

    override fun getState() = this

    override fun loadState(state: TexifyProjectSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}