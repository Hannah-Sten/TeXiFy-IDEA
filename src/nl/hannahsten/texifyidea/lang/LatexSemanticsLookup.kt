package nl.hannahsten.texifyidea.lang

/**
 * Interface for looking up LaTeX semantics entities such as commands and environments.
 */
interface LatexSemanticsLookup {
    fun lookup(name: String): LSemanticEntity?

    /**
     * @param name The name of the command to look up, without the leading backslash.
     */
    fun lookupCommand(name: String): LSemanticCommand? {
        return lookup(name) as? LSemanticCommand
    }

    fun lookupEnv(name: String): LSemanticEnv? {
        return lookup(name) as? LSemanticEnv
    }
}