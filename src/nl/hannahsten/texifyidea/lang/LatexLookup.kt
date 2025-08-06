package nl.hannahsten.texifyidea.lang


interface LatexSemanticEnvLookup{
    fun lookupEnv(name: String): LSemanticEnv?
}
interface LatexSemanticCommandLookup {
    fun lookupCommand(name: String): LSemanticCommand?
}

interface LatexSemanticLookup : LatexSemanticEnvLookup, LatexSemanticCommandLookup {
}