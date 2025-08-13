package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.LatexSemanticsCommandLookup
import nl.hannahsten.texifyidea.util.Log
import kotlin.collections.iterator

object AllPredefinedCommands : LatexSemanticsCommandLookup {

    val allCommands: List<LSemanticCommand> =
        listOf(
            PredefinedPrimitives,
            PredefinedCmdGeneric,
            PredefinedCmdDefinitions,
            PredefinedCmdFiles,
            PredefinedCmdMath,
            PredefinedCmdPairedDelimiters,
            PredefinedCmdMathSymbols,
            PredefinedCmdTextSymbols
        ).flatMap {
            it.allCommands
        }

    val packageToCommands: Map<LatexLib, List<LSemanticCommand>> =
        allCommands.groupBy { it.dependency }.mapValues { it.value }

    val simpleNameLookup = allCommands.associateBy { it.name }

    private const val CHECK_DUPLICATION = true

    init {
        if (CHECK_DUPLICATION) {
            val names = allCommands.groupBy { Pair(it.name, it.dependency) }
            for ((name, commands) in names) {
                if (commands.size > 1) {
                    Log.warn("Duplicate predefined command: ${name.first} in package ${name.second}")
                }
            }
        }
    }

    override fun lookupCommand(name: String): LSemanticCommand? {
        return simpleNameLookup[name]
    }

    val nameToCommands = allCommands.groupBy { it.name }

    fun findAll(name: String): List<LSemanticCommand> {
        return nameToCommands[name] ?: emptyList()
    }
}