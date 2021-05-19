package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.util.xmlb.XmlSerializerUtil

data class TexifyConventionsScheme(
    var myName: String = DEFAULT_SCHEME_NAME,
    var maxSectionSize: Int = 4000,
    @Transient
    val isProjectScheme: Boolean = false
) : com.intellij.openapi.options.Scheme {
    companion object {
        const val DEFAULT_SCHEME_NAME = "Default"
        const val PROJECT_SCHEME_NAME = "Project"
    }

    /**
     * Same as [myName].
     */
    override fun getName() = myName

    fun copySettingsFrom(other: TexifyConventionsScheme) {
        val origName = myName
        XmlSerializerUtil.copyBean(other, this)
        myName = origName
    }
}
