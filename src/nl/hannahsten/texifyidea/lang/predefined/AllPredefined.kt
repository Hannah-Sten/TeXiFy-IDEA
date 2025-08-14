package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LSemanticEntity
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.util.Log
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

object AllPredefined : LatexSemanticsLookup {

    val allEntities = listOf(
        PredefinedPrimitives,
        PredefinedCmdGeneric,
        PredefinedCmdDefinitions,
        PredefinedCmdFiles,
        PredefinedCmdMath,
        PredefinedCmdPairedDelimiters,
        PredefinedCmdMathSymbols,
        PredefinedCmdTextSymbols,
        PredefinedEnvBasic,
        MorePackages
    ).flatMap {
        it.allEntities
    }

    private val packageToEntities: Map<LatexLib, List<LSemanticEntity>> =
        allEntities.groupBy { it.dependency }.mapValues { it.value }

    fun packageToEntities(packageName: LatexLib): List<LSemanticEntity> {
        return packageToEntities[packageName] ?: emptyList()
    }

    private val simpleNameLookup = allEntities.associateBy { it.name }

    override fun lookup(name: String): LSemanticEntity? {
        return simpleNameLookup[name]
    }

    val nameToEntities = allEntities.groupBy { it.name }

    fun findAll(name: String): List<LSemanticEntity> {
        return nameToEntities[name] ?: emptyList()
    }

    fun findAllCommand(name: String): List<LSemanticCommand> {
        return findAll(name).filterIsInstance<LSemanticCommand>()
    }

    fun findAllEnvironment(name: String): List<LSemanticEnv> {
        return findAll(name).filterIsInstance<LSemanticEnv>()
    }

    private const val CHECK_DUPLICATION = true

    init {
        if (CHECK_DUPLICATION) {
            val names = allEntities.groupBy { Pair(it.name, it.dependency) }
            for ((name, commands) in names) {
                if (commands.size > 1) {
                    Log.warn("Duplicate predefined command: ${name.first} in package ${name.second}")
                }
            }
        }
    }
}