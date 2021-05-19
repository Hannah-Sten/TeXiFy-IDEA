package nl.hannahsten.texifyidea.settings.conventions

import nl.hannahsten.texifyidea.lang.DefaultEnvironment.*
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand.*
import nl.hannahsten.texifyidea.lang.commands.LatexListingCommand.LSTINPUTLISTING
import nl.hannahsten.texifyidea.util.magic.env

data class TexifyConventionsScheme(
    var myName: String = DEFAULT_SCHEME_NAME,
    var maxSectionSize: Int = 4000,
    var labelConventions: MutableList<LabelConvention> = mutableListOf(
        LabelConvention(true, LabelConventionType.COMMAND, CHAPTER.command, "ch"),
        LabelConvention(true, LabelConventionType.COMMAND, SECTION.command, "sec"),
        LabelConvention(true, LabelConventionType.COMMAND, SUBSECTION.command, "subsec"),
        LabelConvention(false, LabelConventionType.COMMAND, SUBSUBSECTION.command, "subsubsec"),
        LabelConvention(false, LabelConventionType.COMMAND, PARAGRAPH.command, "par"),
        LabelConvention(false, LabelConventionType.COMMAND, SUBPARAGRAPH.command, "subpar"),
        LabelConvention(false, LabelConventionType.COMMAND, ITEM.command, "itm"),
        LabelConvention(true, LabelConventionType.COMMAND, LSTINPUTLISTING.command, "lst"),
        LabelConvention(true, LabelConventionType.ENVIRONMENT, FIGURE.env, "fig"),
        LabelConvention(true, LabelConventionType.ENVIRONMENT, TABLE.env, "tab"),
        LabelConvention(true, LabelConventionType.ENVIRONMENT, EQUATION.env, "eq"),
        LabelConvention(true, LabelConventionType.ENVIRONMENT, ALGORITHM.env, "alg"),
        LabelConvention(true, LabelConventionType.ENVIRONMENT, LISTINGS.env, "lst"),
        LabelConvention(true, LabelConventionType.ENVIRONMENT, VERBATIM_CAPITAL.env, "verb"),
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
