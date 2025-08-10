package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.util.Log
import kotlin.collections.iterator

object AllPredefinedCommands {

    val allCommands: List<LSemanticCommand> =
        listOf(
            PredefinedBasicCommands,
            PredefinedFileCommands,
            PredefinedMathCommands,
            PredefinedMathSymbols,
            PredefinedTextSymbols
        ).flatMap {
            it.allCommands
        }

    val packageToCommands: Map<String, List<LSemanticCommand>> =
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

    val regularCommandDef = PredefinedBasicCommands.definitionOfCommand.associateBy { it.name }
    val regularEnvironmentDef = PredefinedBasicCommands.definitionOfEnvironment.associateBy { it.name }
}