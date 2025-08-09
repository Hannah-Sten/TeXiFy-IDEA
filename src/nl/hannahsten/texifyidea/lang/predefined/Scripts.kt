package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.lang.commands.Argument
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.OptionalArgument
import nl.hannahsten.texifyidea.lang.commands.RequiredArgument
import kotlin.collections.component1
import kotlin.collections.component2


fun parseFor(s: Collection<LatexCommand>) {
    // Example usage of LatexOperatorCommand
    // group by dependency

    val groupedByDependency = s.groupBy { it.dependency.name }
    groupedByDependency.forEach { (dependency, commands) ->
        println("packageOf(\"$dependency\")")
        commands.sortedBy { it.command }
            .forEach { command ->
                val cmdEscaped = command.command.replace("\\", "\\\\")
                if (command.arguments.isEmpty()) {
                    if (command.display != null) {
                        val display = command.display?.let {
                            "\"${it}\""
                        } ?: "null"
                        println("symbol(\"${cmdEscaped}\", $display)")
                    }
                    else {
                        println("+\"${cmdEscaped}\"")
                    }
                }
                else {
                    val arguments = command.arguments

                    val argsString = arguments.joinToString(", ") {
                        val name = "\"${it.name}\""
                        if (it::class == RequiredArgument::class) {
                            "$name.required"
                        }
                        else if (it is OptionalArgument) {
                            "$name.optional"
                        }
                        else {
                            "TODO(\"${name}\")"
                        }
                    }
                    if (arguments.any { it is RequiredArgument && it.type == Argument.Type.FILE }) {
                        if (command.description.isNotEmpty()) {
                            println(
                                "\"${cmdEscaped}\".cmd($argsString) { \"${command.description}\" }"
                            )
                        }
                        else {
                            println(
                                "\"${cmdEscaped}\".cmd($argsString)"
                            )
                        }
                    }
                }
            }
        println()
    }
}
