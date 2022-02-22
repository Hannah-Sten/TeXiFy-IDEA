package nl.hannahsten.texifyidea.settings.conventions

import nl.hannahsten.texifyidea.lang.DefaultEnvironment.*
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand.*
import nl.hannahsten.texifyidea.lang.commands.LatexListingCommand.LSTINPUTLISTING
import nl.hannahsten.texifyidea.util.magic.env

/**
 * A scheme instance for storing settings regarding Texify conventions. Default values of this class represent the
 * default settings
 *
 * Scheme instances generally store settings for a specific scope. Concrete scheme implementations decide on which
 * scopes make sense. For example, a scheme could represent different color settings for code or different inspection
 * rules. In the context of Texify conventions, a scheme stores either project or IDE level conventions.
 *
 * Instances must be serializable since they are persisted as part of [TexifyConventionsGlobalState] or
 * [TexifyConventionsProjectState]. Changing the properties to "val" silently fails serialization.
 */
data class TexifyConventionsScheme(

    /**
     * The name of the scheme
     */
    var myName: String = DEFAULT_SCHEME_NAME,

    /**
     * The maximum section size before the corresponding inspection issues a warning.
     */
    var maxSectionSize: Int = 4000,

    /**
     * List of configured conventions.
     * The default conventions mirror the settings currently read from *Magic classes and the existing settings UI.
     * For example, which label conventions are enabled by default corresponds to the minimum section level which
     * should receive a label, but provides more fine-grained control.
     */
    var labelConventions: MutableList<LabelConvention> = mutableListOf(
        LabelConvention(true, LabelConventionType.COMMAND, PART.command, "part"),
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

    /**
     * Copy all settings except the name from the given scheme. This method is useful when you want to transfer settings
     * from one instance to another.
     */
    fun copyFrom(scheme: TexifyConventionsScheme) {
        if (scheme != this) {
            maxSectionSize = scheme.maxSectionSize
            labelConventions.clear()
            labelConventions.addAll(scheme.labelConventions)
        }
    }
}
