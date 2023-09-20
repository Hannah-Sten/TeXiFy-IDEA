package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.LatexRegularCommand
import nl.hannahsten.texifyidea.lang.commands.RequiredFileArgument
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.files.*
import nl.hannahsten.texifyidea.util.labels.getLabelDefinitionCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.magic.PackageMagic
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
 * Inserts a usepackage statement for the given package in a certain file.
 *
 * @param file
 *          The file to add the usepackage statement to.
 * @param packageName
 *          The name of the package to insert.
 * @param parameters
 *          Parameters to add to the statement, `null` or empty string for no parameters.
 */
fun insertCommandDefinition(file: PsiFile, commandText: String, newCommandName: String = "mycommand"): PsiElement? {
    if (!file.isWritable) return null

    val commands = file.commandsInFile()

    var last: LatexCommands? = null
    for (cmd in commands) {
        if (cmd.commandToken.text == "\\newcommand") {
            last = cmd
        } else if (cmd.commandToken.text == "\\usepackage") {
            last = cmd
        } else if (cmd.commandToken.text == "\\begin" && cmd.requiredParameter(0) == "document") {
            last = cmd
            break
        }
    }

    // The anchor after which the new element will be inserted
    val anchorAfter: PsiElement?

    // When there are no usepackage commands: insert below documentclass.
    if (last == null) {
        val classHuh = commands.asSequence()
            .filter { cmd ->
                "\\documentclass" == cmd.commandToken
                    .text || "\\LoadClass" == cmd.commandToken.text
            }
            .firstOrNull()
        if (classHuh != null) {
            anchorAfter = classHuh
        }
        else {
            // No other sensible location can be found
            anchorAfter = null
        }
    }
    // Otherwise, insert below the lowest usepackage.
    else {
        anchorAfter = last
    }

    val blockingNames = file.definitions().filter { it.commandToken.text.matches("${newCommandName}\\d*".toRegex()) }

    val nonConflictingName = "${newCommandName}${if (blockingNames.isEmpty()) "" else blockingNames.size.toString()}"
    val command = "\\newcommand{\\$nonConflictingName}{${commandText}}";

    val newChild = LatexPsiHelper(file.project).createFromText(command).firstChild
    val newNode = newChild.node

    // Don't run in a write action, as that will produce a SideEffectsNotAllowedException for INVOKE_LATER

    // Avoid "Attempt to modify PSI for non-committed Document"
    // https://www.jetbrains.org/intellij/sdk/docs/basics/architectural_overview/modifying_psi.html?search=refac#combining-psi-and-document-modifications
    PsiDocumentManager.getInstance(file.project)
        .doPostponedOperationsAndUnblockDocument(file.document() ?: return null)
    PsiDocumentManager.getInstance(file.project).commitDocument(file.document() ?: return null)

    runWriteAction {
        // Avoid NPE, see #3083 (cause unknown)
        if (anchorAfter != null && com.intellij.psi.impl.source.tree.TreeUtil.getFileElement(anchorAfter.parent.node) != null) {
            val anchorBefore = anchorAfter.node.treeNext
            val newLine = LatexPsiHelper(file.project).createFromText("\n\n").firstChild.node
            anchorAfter.parent.node.addChild(newLine, anchorBefore)
            anchorAfter.parent.node.addChild(newNode, anchorBefore)
        }
        else {
            // Insert at beginning
            file.node.addChild(newNode, file.firstChild.node)
        }
    }

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
        val commandExpansion = LatexCommandsIndex.getCommandsByNames(file ?: return null, *CommandMagic.commandDefinitionsAndRedefinitions.toTypedArray())
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
