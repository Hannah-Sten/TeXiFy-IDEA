package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.Dependend
import nl.hannahsten.texifyidea.lang.Described
import nl.hannahsten.texifyidea.lang.LatexPackage
import kotlin.reflect.KClass

/**
 * @author Hannah Schellekens, Sten Wessel
 */
interface LatexCommand : Described, Dependend {

    /**
     * Uniquely identifies the command, when two commands are the same, but from different packages, the identifier
     * should be different.
     */
    val identifier: String
        get() = commandWithSlash

    override val description: String
        get() = identifier

    /**
     * Get the name of the command without the first backslash.
     */
    val command: String

    val commandWithSlash: String
        get() = "\\$command"

    /**
     * Get the display value of the command.
     */
    val display: String?

    /**
     * Get all the command arguments.
     */
    val arguments: Array<out Argument>

    /**
     * Whether this command must be used in math mode (`true`).
     */
    val isMathMode: Boolean

    /**
     * Concatenates all arguments to each other.
     *
     * @return e.g. `{ARG1}{ARG2}[ARG3]`
     */
    @Suppress("KDocUnresolvedReference")
    fun getArgumentsDisplay(): String {
        val sb = StringBuilder()
        for (arg in arguments) {
            sb.append(arg.toString())
        }

        return sb.toString()
    }

    /**
     * Checks whether `{}` must be automatically inserted in the auto complete.
     *
     * @return `true` to insert automatically, `false` not to insert.
     */
    fun autoInsertRequired() = arguments.filterIsInstance<RequiredArgument>().isNotEmpty()

    @Suppress("UNCHECKED_CAST")
    fun <T : Argument> getArgumentsOf(clazz: KClass<T>): List<T> {
        return arguments.asSequence()
            .filter { clazz.java.isAssignableFrom(it.javaClass) }
            .mapNotNull { it as? T }
            .toList()
    }

    fun <T : Argument> getArgumentsOf(clazz: Class<T>) = getArgumentsOf(clazz.kotlin)

    /**
     * Gets the required arguments of this command.
     */
    val requiredArguments: List<RequiredArgument>
        get() = arguments.filterIsInstance<RequiredArgument>()
}

data class LatexCommandImpl(
    override val commandWithSlash: String,
    override val dependency: LatexPackage,
    override val display: String? = null,
    override val description: String = "",
    override val arguments: Array<out Argument> = emptyArray(),
    override val isMathMode: Boolean = false
) : LatexCommand {
    override val command: String = commandWithSlash.substring(1)
//    override val commandWithSlash: String = "\\$command" // store it for performance reasons

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LatexCommandImpl

        return identifier == other.identifier
    }

    override fun hashCode(): Int {
        return identifier.hashCode()
    }
}

internal fun String.asRequired(type: Argument.Type = Argument.Type.NORMAL) = RequiredArgument(this, type)

internal fun String.asOptional(type: Argument.Type = Argument.Type.NORMAL) = OptionalArgument(this, type)
