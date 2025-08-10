package nl.hannahsten.texifyidea.lang

interface LatexSemanticEnvLookup {
    fun lookupEnv(name: String): LSemanticEnv?
}
interface LatexSemanticCommandLookup {

    /**
     * @param name The name of the command to look up, without the leading backslash.
     */
    fun lookupCommand(name: String): LSemanticCommand?
}

interface LatexSemanticLookup : LatexSemanticEnvLookup, LatexSemanticCommandLookup