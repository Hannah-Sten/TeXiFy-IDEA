package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.lang.*
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.extractSubParameterRanges
import nl.hannahsten.texifyidea.psi.readFirstParam
import nl.hannahsten.texifyidea.reference.InputFileReference

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
            .filter { command -> command.arguments.any { it is RequiredFileArgument || it is RequiredPicturePathArgument }}
            .map { "\\" + it.command }
            .toSet()
}

/**
 * Check if the command includes other files, and if so return [InputFileReference] instances for them.
 */
fun LatexCommands.getFileArgumentsReferences(): List<InputFileReference> {
    val inputFileReferences = mutableListOf<InputFileReference>()

    // There may be multiple commands with this name, just guess the first one
    val command = LatexCommand.lookup(this.name)?.firstOrNull() ?: return emptyList()

    // Arguments from the LatexCommand (so the command as hardcoded in e.g. LatexRegularCommand)
    val requiredArguments = command.arguments.mapNotNull { it as? RequiredArgument }

    val firstParam = readFirstParam(this) ?: return emptyList()
    val subParamRanges = extractSubParameterRanges(firstParam)

    // Loop through arguments
    for (i in subParamRanges.indices) {

        // Find the corresponding requiredArgument
        val requiredArgument = if (i < requiredArguments.size) requiredArguments[i] else requiredArguments.last { it is RequiredFileArgument }

        // Check if the actual argument is a file argument or continue with the next argument
        val fileArgument = requiredArgument as? RequiredFileArgument ?: continue
        val extensions = fileArgument.supportedExtensions

        val range = subParamRanges[i].shiftRight(firstParam.textOffset - this.textOffset)

        inputFileReferences.add(InputFileReference(this, range, extensions, fileArgument.defaultExtension))
    }

    return inputFileReferences
}