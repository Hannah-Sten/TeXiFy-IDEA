package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.util.Log

object PredefinedCommands {

    val allCommands: List<LSemanticCommand> =
        listOf(
            NewLatexBasicCommands,
            NewLatexFileCommands,
            NewLatexMathCommands,
            NewLatexMathSymbols,
            NewLatexTextSymbols
        ).flatMap {
            it.allCommands
        }


    val packageToCommands: Map<String, List<LSemanticCommand>> =
        allCommands.groupBy { it.dependency }.mapValues { it.value }


    val nameToCommands: Map<String, List<LSemanticCommand>> =
        allCommands.groupBy { it.name }


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
}