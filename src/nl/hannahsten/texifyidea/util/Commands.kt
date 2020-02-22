package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.lang.LatexCommand
import nl.hannahsten.texifyidea.lang.RequiredArgument
import nl.hannahsten.texifyidea.lang.RequiredFileArgument
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
 * Check if the command has required file arguments, and if so return [InputFileReference] instances for them.
 */
fun LatexCommands.getFileArgumentsReferences(): List<InputFileReference> {
    val inputFileReferences = mutableListOf<InputFileReference>()

    // There may be multiple commands with this name, just guess the first one
    val command = LatexCommand.lookup(this.name)?.firstOrNull() ?: return emptyList()

    // Arguments from the LatexCommand (so the command as hardcoded in e.g. LatexRegularCommand)
    val requiredArguments = command.arguments.mapNotNull { it as? RequiredArgument }

    // Actual parameters from the current LatexCommands (so the command as in the PSI tree)
    val requiredParameters = this.parameterList.filter { it.requiredParam != null }

    val firstParam = readFirstParam(this) ?: return emptyList()
    val subParamRanges = extractSubParameterRanges(firstParam)

    // Loop through arguments
    for (i in requiredArguments.indices) {
        // When there are more required arguments than actually present break the loop
        if (i >= requiredArguments.size || i >= subParamRanges.size) {
            break
        }

        // Check if the actual argument is a file argument or continue with the next argument
        val parameter = requiredParameters[i]
        val fileArgument = parameter as? RequiredFileArgument ?: continue
        val extensions = fileArgument.supportedExtensions

        val range = subParamRanges[i]

        inputFileReferences.add(InputFileReference(this, range, extensions))
    }

    return inputFileReferences
}