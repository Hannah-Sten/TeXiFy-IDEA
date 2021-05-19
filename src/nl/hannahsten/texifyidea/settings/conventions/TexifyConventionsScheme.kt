package nl.hannahsten.texifyidea.settings.conventions

data class TexifyConventionsScheme(
    var myName: String = DEFAULT_SCHEME_NAME,
    var maxSectionSize: Int = 4000,
    var labelConventions: MutableList<LabelConvention> = mutableListOf(
        LabelConvention(false, LabelConventionType.ENVIRONMENT, "lstlisting", "lst"),
        LabelConvention(true, LabelConventionType.COMMAND, "section", "sec")
    )
) : com.intellij.openapi.options.Scheme {
    val isProjectScheme: Boolean
        get() = name == PROJECT_SCHEME_NAME

    companion object {
        const val DEFAULT_SCHEME_NAME = "Default"
        const val PROJECT_SCHEME_NAME = "Project"
    }

    fun deepCopy(): TexifyConventionsScheme =
        copy(labelConventions = labelConventions.map { it.copy() }.toMutableList())

    /**
     * Same as [myName].
     */
    override fun getName() = myName

}
