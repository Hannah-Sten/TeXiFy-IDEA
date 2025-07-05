package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
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
import nl.hannahsten.texifyidea.util.parser.traverseTyped

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
        else if (cmd.name == LatexGenericRegularCommand.BEGIN.cmd && cmd.requiredParameterText(0) == DefaultEnvironment.DOCUMENT.environmentName) {
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
 * Expand custom commands in a given text once, using its definition in the index.
 */
fun expandCommandsOnce(inputText: String, project: Project, file: PsiFile?): String? {
    if(file == null) return null
    var text = inputText
    // Get all the commands that are used in the input text.
    val commandsInText = LatexPsiHelper(project).createFromText(inputText).traverseTyped<LatexCommands>()
    for (command in commandsInText) {
        // Expand the command once, and replace the command with the expanded text
        val commandExpansion = NewCommandsIndex.getByNames(CommandMagic.commandDefinitionsAndRedefinitions, file)
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
