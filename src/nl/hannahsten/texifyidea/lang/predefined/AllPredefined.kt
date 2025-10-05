package nl.hannahsten.texifyidea.lang.predefined

import com.intellij.openapi.application.ApplicationManager
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

    init {
        if (ApplicationManager.getApplication().isInternal) {
            val names = allEntities.groupBy { it }
            for ((item, commands) in names) {
                if (commands.size > 1) {
                    Log.warn("Duplicate predefined items: ${item.name}(${item.dependency}): ${commands.joinToString()}")
                }
            }
        }
    }
}