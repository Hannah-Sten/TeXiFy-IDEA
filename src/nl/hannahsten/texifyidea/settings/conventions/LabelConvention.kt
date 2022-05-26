package nl.hannahsten.texifyidea.settings.conventions

/**
 * A label convention stores the Texify conventions for a specific label type.
 *
 * The parameters need default values for XML serialization. Turning any of them into a "val" leads to the serialization
 * silently failing.
 */
data class LabelConvention(

    /**
     * Whether the convention is enabled
     */
    var enabled: Boolean = false,

    /**
     * The type of Latex element this convention applies to
     */
    var type: LabelConventionType? = null,

    /**
     * The command name this convention applies to
     */
    var name: String? = null,

    /**
     * The prefix to use for an inserted label
     */
    var prefix: String? = null
)

enum class LabelConventionType {

    ENVIRONMENT,
    COMMAND
}