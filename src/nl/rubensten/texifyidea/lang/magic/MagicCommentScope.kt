package nl.rubensten.texifyidea.lang.magic

/**
 * A list of all scopes on which magic comments can operate.
 *
 * Magic comments will always operate on (or target) the first instance of the given scope
 *
 * @author Ruben Schellekens
 */
enum class MagicCommentScope {

    /**
     * Targets the complete file.
     */
    FILE,

    /**
     * Targets the following environment.
     */
    ENVIRONMENT,

    /**
     * Targets the following command.
     */
    COMMAND,

    /**
     * Targets the following content group.
     */
    GROUP;

    companion object {

        /**
         * Contains all comment scopes.
         */
        @JvmStatic
        val ALL_SCOPES = values().toSet()
    }
}

/**
 * A set of scopes that only contains this scope.
 */
fun MagicCommentScope.singleScope() = setOf(this)