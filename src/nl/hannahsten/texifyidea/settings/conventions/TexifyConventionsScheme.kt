package nl.hannahsten.texifyidea.settings

import com.intellij.util.xmlb.XmlSerializerUtil

abstract class TexifyConventionsScheme() : com.intellij.openapi.options.Scheme {
    companion object {
        const val DEFAULT_SCHEME_NAME = "Default"
        const val PROJECT_SCHEME_NAME = "Project"
    }

    abstract var maxSectionSize: Long

    /**
     * The name of the scheme, used to identify it.
     */
    abstract var myName: String

    /**
     * Same as [myName].
     */
    override fun getName() = myName

    fun copySettingsFrom(other: TexifyConventionsScheme) {
        val origName = myName
        XmlSerializerUtil.copyBean(other, this)
        myName = origName
    }

    abstract fun isProjectScheme(): Boolean
}

data class TexifyConventionsGlobalScheme(
    override var myName: String = DEFAULT_SCHEME_NAME,
    override var maxSectionSize: Long = 4000
) : TexifyConventionsScheme() {
    override fun isProjectScheme() = false
}

data class TexifyConventionsProjectScheme(
    override var myName: String = PROJECT_SCHEME_NAME,
    override var maxSectionSize: Long = 4000
) : TexifyConventionsScheme() {
    override fun isProjectScheme() = true
}


