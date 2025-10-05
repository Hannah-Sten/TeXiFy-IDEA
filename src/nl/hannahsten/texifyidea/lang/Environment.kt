package nl.hannahsten.texifyidea.lang

import nl.hannahsten.texifyidea.lang.commands.Argument

/**
 * @author Hannah Schellekens
 */
interface Environment : Dependend, Described {

    /**
     * Get what type of context this enviroment has inside.
     */
    val context: Context

    /**
     * Get the contents that must be placed into the environment just after it has been
     * inserted using the auto complete.
     */
    val initialContents: String

    /**
     * Get the name of the environment.
     */
    val environmentName: String

    /**
     * Get all the environment myArguments.
     */
    val arguments: Array<out Argument>

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
     * @author Hannah Schellekens
     */
    enum class Context {

        NORMAL,
        MATH,
        COMMENT
    }
}
