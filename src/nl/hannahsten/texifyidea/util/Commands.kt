package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.lang.RequiredFileArgument
import nl.hannahsten.texifyidea.lang.RequiredPicturePathArgument
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * Finds all defined commands within the project.
 *
 * @return The found commands.
 */
fun Project.findCommandDefinitions(): Collection<LatexCommands> {
    return LatexDefinitionIndex.getItems(this).filter {
        it.name in Magic.Command.commandDefinitions
    }
}

/**
 * Get all commands that include other files, including backslashes.
 */
fun getIncludeCommands(): Set<String> {
    return LatexRegularCommand.values()
            .filter { command -> command.arguments.any { it is RequiredFileArgument || it is RequiredPicturePathArgument } }
            .map { "\\" + it.command }
            .toSet()
}
