package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.NewDefinitionIndex
import nl.hannahsten.texifyidea.lang.predefined.CommandNames
import nl.hannahsten.texifyidea.lang.predefined.EnvironmentNames
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.util.PackageUtils.getDefaultInsertAnchor
import nl.hannahsten.texifyidea.util.files.definitions
import nl.hannahsten.texifyidea.util.parser.getRequiredArgumentValueByName
import nl.hannahsten.texifyidea.util.parser.traverseTyped

/**
 * Inserts a custom command definition.
 */
fun insertCommandDefinition(file: PsiFile, commandText: String, newCommandName: String = "mycommand"): PsiElement? {
    if (!file.isWritable) return null

    val commands = file.traverseCommands()

    var last: LatexCommands? = null
    for (cmd in commands) {
        if (cmd.name == CommandNames.NEW_COMMAND) {
            last = cmd
        }
        else if (cmd.name == CommandNames.USE_PACKAGE) {
            last = cmd
        }
        else if (cmd.name == CommandNames.BEGIN && cmd.requiredParameterText(0) == EnvironmentNames.DOCUMENT) {
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
 *
 * @param parentFile The file which includes [file].
 */
fun expandCommandsOnce(inputText: String, project: Project, file: VirtualFile?, parentFile: VirtualFile? = null): String {
    file ?: return inputText
    if (!inputText.contains('\\')) return inputText // No commands to expand, return the text as is
    var text = inputText
    // Get all the commands that are used in the input text.
    val psi = LatexPsiHelper(project).createFromText(inputText)
    val commandsInText = psi.traverseTyped<LatexCommands>()
    for (command in commandsInText) {
        // Expand the command once, and replace the command with the expanded text
        val name = command.name ?: continue
        expandCurrfileCommand(command, file, parentFile)?.let {
            text = text.replace(command.text, it)
            continue
        }
        val definitionCommand = NewDefinitionIndex.getByName(name, project, file).firstOrNull() ?: continue
        definitionCommand.getRequiredArgumentValueByName("code")
            ?.let { commandExpansion ->
                text = text.replace(command.text, commandExpansion)
            }
    }
    return text
}

/**
 * The currfile package provides various commands which resolve to parts of the current file path.
 *
 * @param parentFile The file which includes [file].
 */
fun expandCurrfileCommand(command: LatexCommands, file: VirtualFile, parentFile: VirtualFile? = null): String? = when (command.name) {
    CommandNames.CURRFILE_DIR,
    CommandNames.CURRFILE_ABS_DIR -> file.parent?.path ?: ""
    CommandNames.CURRFILE_BASE -> file.nameWithoutExtension
    CommandNames.CURRFILE_EXT -> file.extension ?: ""
    CommandNames.CURRFILE_NAME -> file.name
    CommandNames.CURRFILE_PATH -> file.path
    CommandNames.CURRFILE_ABS_PATH -> file.path

    CommandNames.PARENTFILE_DIR,
    CommandNames.PARENTFILE_ABS_DIR -> parentFile?.parent?.path ?: ""
    CommandNames.PARENTFILE_BASE -> parentFile?.nameWithoutExtension ?: ""
    CommandNames.PARENTFILE_EXT -> parentFile?.extension ?: ""
    CommandNames.PARENTFILE_NAME -> parentFile?.name ?: ""
    CommandNames.PARENTFILE_PATH -> parentFile?.path ?: ""
    CommandNames.PARENTFILE_ABS_PATH -> parentFile?.path ?: ""
    else -> null
}