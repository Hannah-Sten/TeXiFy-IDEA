package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.LatexRegularCommand
import nl.hannahsten.texifyidea.lang.commands.RequiredFileArgument
import nl.hannahsten.texifyidea.lang.commands.RequiredPicturePathArgument
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import java.util.stream.Collectors

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

/**
 * Get the index of the parameter in the command. This includes both required and optional parameters.
 */
fun LatexParameter.indexOf() = indexOfChildByType<LatexParameter, LatexCommands>()

/**
 * Get the predefined [LatexCommand] if a matching one could be found.
 * When multiple versions are available, only a random one will be selected.
 */
fun LatexCommands.defaultCommand(): LatexCommand? {
    return LatexCommand.lookup(this.name)?.firstOrNull()
}

fun LatexCommands.isFigureLabel(): Boolean =
    name in project.getLabelDefinitionCommands() && inDirectEnvironment(EnvironmentMagic.figures)


fun getCommandsInFiles(files: MutableSet<PsiFile>, originalFile: PsiFile): Collection<LatexCommands> {
    val project = originalFile.project
    val searchFiles = files.stream()
        .map { obj: PsiFile -> obj.virtualFile }
        .collect(Collectors.toSet())
    searchFiles.add(originalFile.virtualFile)
    val scope = GlobalSearchScope.filesScope(project, searchFiles)
    return LatexCommandsIndex.getItems(project, scope)
}
