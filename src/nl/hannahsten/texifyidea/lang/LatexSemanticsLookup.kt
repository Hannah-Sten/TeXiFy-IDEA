package nl.hannahsten.texifyidea.lang


interface LatexSemanticsCommandLookup {
    /**
     * @param name **without** the leading backslash.
     */
    fun lookupCommand(name: String): LSemanticCommand?
}

interface LatexSemanticsEnvLookup {
    fun lookupEnv(name: String): LSemanticEnv?
}

/**
 * Interface for looking up LaTeX semantics entities such as commands and environments.
 */
interface LatexSemanticsLookup : LatexSemanticsCommandLookup, LatexSemanticsEnvLookup {

    /**
     * Looks up a semantic entity by its name.
     *
     * @param name The name of the entity, such as a command or environment, without the leading backslash for commands.
     * @return The [LSemanticEntity] if found, or null if not found.
     */
    fun lookup(name: String): LSemanticEntity?

    override fun lookupCommand(name: String): LSemanticCommand? {
        return lookup(name) as? LSemanticCommand
    }

    override fun lookupEnv(name: String): LSemanticEnv? {
        return lookup(name) as? LSemanticEnv
    }
}