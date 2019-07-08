package nl.hannahsten.texifyidea.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler

/**
 * todo
 * Project-level values are stored in the workspace file.
 */
//@State(name = "TexifyProjectSettings")
// storages optional for project settings
@State(name = "TexifyProjectSettings", storages = [(Storage("texifyProjectSettings.xml"))])
@Storage("texifyProjectSettings.xml")
//@Storage(StoragePathMacros.WORKSPACE_FILE)
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