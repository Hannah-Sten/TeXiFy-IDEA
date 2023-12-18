package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.lang.commands.*
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.PackageUtils.getDefaultInsertAnchor
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.definitions
import nl.hannahsten.texifyidea.util.labels.getLabelDefinitionCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.*
import java.util.stream.Collectors

/**
 * Finds all defined commands within the project.
 *
 * @return The found commands.
 */
fun Project.findCommandDefinitions(): Collection<LatexCommands> {
    return LatexDefinitionIndex.getItems(this).filter {
        it.isCommandDefinition()
    }
}

/**
 * Get all commands that include other files, including backslashes.
 */
fun getIncludeCommands(): Set<String> {
    return LatexRegularCommand.values()
        .filter { command -> command.arguments.any { it is RequiredFileArgument } }
        .map { "\\" + it.command }
        .toSet()
}

/**
 * Inserts a custom c custom command definition.
 */
fun insertCommandDefinition(file: PsiFile, commandText: String, newCommandName: String = "mycommand"): PsiElement? {
    if (!file.isWritable) return null

    val commands = file.commandsInFile()

    var last: LatexCommands? = null
    for (cmd in commands) {
        if (cmd.name == LatexNewDefinitionCommand.NEWCOMMAND.cmd) {
            last = cmd
        }
        else if (cmd.name == LatexGenericRegularCommand.USEPACKAGE.cmd) {
            last = cmd
        }
        else if (cmd.name == LatexGenericRegularCommand.BEGIN.cmd && cmd.requiredParameter(0) == "document") {
            last = cmd
            break
        }
    }

    val blockingNames = file.definitions().filter { it.commandToken.text.matches("${newCommandName}\\d*".toRegex()) }

    val nonConflictingName = "${newCommandName}${if (blockingNames.isEmpty()) "" else blockingNames.size.toString()}"
    val command = "\\newcommand{\\$nonConflictingName}{$commandText}\n"

    val newChild = LatexPsiHelper(file.project).createFromText(command).firstChild
    val newNode = newChild.node

    // The anchor after which the new element will be inserted
    // When there are no usepackage commands: insert below documentclass.
    val (anchorAfter, _) = getDefaultInsertAnchor(commands, last)

    PackageUtils.insertNodeAfterAnchor(file, anchorAfter, prependNewLine = true, newNode, prependBlankLine = true)

    return newChild
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
        val commandExpansion = LatexCommandsIndex.getCommandsByNames(
            file ?: return null,
            *CommandMagic.commandDefinitionsAndRedefinitions.toTypedArray()
        )
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
