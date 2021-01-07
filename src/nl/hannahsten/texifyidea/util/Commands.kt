package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.lang.RequiredFileArgument
import nl.hannahsten.texifyidea.lang.RequiredPicturePathArgument
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * Finds all defined commands within the project.
 *
 * @return The found commands.
 */
fun Project.findCommandDefinitions(): Collection<LatexCommands> {
    return LatexDefinitionIndex.getItems(this).filter {
        it.name in CommandMagic.commandDefinitions
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

/**
 * Expand custom commands in a given text once, using its definition in the [LatexCommandsIndex].
 */
fun expandCommandsOnce(inputText: String, project: Project, file: PsiFile?): String? {
    var text = inputText
    // Get all the commands that are used in the input text.
    val commandsInText = LatexPsiHelper(project).createFromText(inputText).childrenOfType(LatexCommands::class)

    for (command in commandsInText) {
        // Expand the command once, and replace the command with the expanded text
        val commandExpansion = LatexCommandsIndex.getCommandsByNames(file ?: return null, *CommandMagic.commandDefinitions.toTypedArray())
                .firstOrNull { it.getRequiredArgumentValueByName("cmd") == command.text }
                ?.getRequiredArgumentValueByName("def")
        text = text.replace(command.text, commandExpansion ?: command.text)
    }
    return text
}
