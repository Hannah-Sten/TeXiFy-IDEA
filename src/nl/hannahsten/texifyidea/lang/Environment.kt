package nl.hannahsten.texifyidea.lang

/**
 * @author Hannah Schellekens
 */
interface Environment : Dependend {

    companion object {

        /**
         * Looks up a default environment by the given name.
         *
         * @param environmentName
         *              The name of the environment object to get.
         * @return The [DefaultEnvironment] with the given name, or `null` when it couldn't
         * be found.
         */
        fun lookup(environmentName: String) = DefaultEnvironment[environmentName]

        /**
         * @see [lookup]
         */
        operator fun get(environmentName: String) = lookup(environmentName)
    }

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
     * @author Hannah Schellekens
     */
    enum class Context {
        NORMAL,
        MATH,
        COMMENT
    }
}
