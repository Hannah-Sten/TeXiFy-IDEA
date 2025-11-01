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
 *
 * @author Ezrnest
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

    fun allEntitiesSeq(): Sequence<LSemanticEntity>

    /**
     * Find all entities that introduce the given context.
     */
    fun findByRelatedContext(context: LatexContext): List<LSemanticEntity>
}

abstract class CachedLatexSemanticsLookup : LatexSemanticsLookup {

    protected val contextInverseSearch: MutableMap<LatexContext, MutableList<LSemanticEntity>> by lazy {
        buildContextInverseSearch(allEntitiesSeq())
    }

    final override fun findByRelatedContext(context: LatexContext): List<LSemanticEntity> {
        return contextInverseSearch[context] ?: emptyList()
    }

    companion object {
        fun buildContextInverseSearch(entities: Sequence<LSemanticEntity>): MutableMap<LatexContext, MutableList<LSemanticEntity>> {
            val contextInverseSearch = mutableMapOf<LatexContext, MutableList<LSemanticEntity>>()
            fun append(contexts: LContextSet, entity: LSemanticEntity) {
                for (context in contexts) {
                    contextInverseSearch.getOrPut(context) { mutableListOf() }.add(entity)
                }
            }

            for (entity in entities) {
                when (entity) {
                    is LSemanticCommand -> {
                        for (arg in entity.arguments) {
                            append(arg.contextSignature.introducedContexts, entity)
                        }
                    }

                    is LSemanticEnv -> {
                        append(entity.contextSignature.introducedContexts, entity)
                        for (arg in entity.arguments) {
                            append(arg.contextSignature.introducedContexts, entity)
                        }
                    }
                }
            }
            return contextInverseSearch
        }
    }
}