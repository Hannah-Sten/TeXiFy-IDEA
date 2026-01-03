package nl.hannahsten.texifyidea.util.parser

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.lang.predefined.AllPredefined
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.lineIndentation
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * Checks whether the given LaTeX commands is a definition or not.
 *
 * This is either a command definition or an environment definition. Does not count redefinitions.
 *
 * @return `true` if the command is an environment definition or a command definition, `false` when the command is
 *         `null` or otherwise.
 */
fun LatexCommands?.isDefinition() = this != null && this.name in CommandMagic.definitions

/**
 * Checks whether the given LaTeX commands is a (re)definition or not.
 *
 * This is either a command definition or an environment (re)definition.
 *
 * @return `true` if the command is an environment (re)definition or a command (re)definition, `false` when the command is
 *         `null` or otherwise.
 */
fun LatexCommands?.isDefinitionOrRedefinition() = this != null &&
    (
        this.name in CommandMagic.commandDefinitionsAndRedefinitions ||
            this.name in CommandMagic.environmentDefinitions ||
            this.name in CommandMagic.environmentRedefinitions
        )

/**
 * Checks whether the given LaTeX commands is an environment definition or not.
 *
 * @return `true` if the command is an environment definition, `false` when the command is `null` or otherwise.
 */
fun LatexCommands?.isEnvironmentDefinition(): Boolean = this != null && name in CommandMagic.environmentDefinitions

/**
 * Get the command that gets defined by a definition (`\let` or `\def` command).
 */
fun LatexCommands.definitionCommand(): LatexCommands? = forcedFirstRequiredParameterAsCommand()

/**
 * Get the name of the command that is defined by `this` command.
 */
fun LatexCommands.definedCommandName() = definitionCommand()?.name

/**
 * Looks for the next command relative to the given command.
 *
 * @return The next command in the file, or `null` when there is no such command.
 */
fun LatexCommands.nextCommand(): LatexCommands? {
    val content = parentOfType(LatexNoMathContent::class) ?: return null
    val next = content.nextSiblingIgnoreWhitespace() as? LatexNoMathContent
        ?: return null
    return next.findFirstChildOfType(LatexCommands::class)
}

/**
 * Looks for the previous command relative to the given command.
 *
 * @return The previous command in the file, or `null` when there is no such command.
 */
fun PsiElement.previousCommand(): LatexCommands? {
    val content = parentOfType(LatexNoMathContent::class) ?: return null
    val previous = content.previousSiblingIgnoreWhitespace() as? LatexNoMathContent
        ?: return null
    return previous.findFirstChildOfType(LatexCommands::class)
}

/**
 * Get the value of the named [argument] given in `this` command.
 */
fun LatexCommands.getRequiredArgumentValueByName(argument: String, lookup: LatexSemanticsLookup = AllPredefined): String? {
    // Find all pre-defined commands that define `this` command.
    val semantics = lookup.lookupCommandPsi(this) ?: return null
    val idx = LArgument.indexOfFirstRequired(semantics.arguments) { it.name == argument }
    if (idx == -1) return null
    return this.requiredParameterText(idx)
}

/**
 * Get the value of the named optional [argument] given in `this` command.
 *
 * @return null when the optional argument is not given.
 */
fun LatexCommands.getOptionalArgumentValueByName(argument: String, lookup: LatexSemanticsLookup = AllPredefined): String? {
    // Find all pre-defined commands that define `this` command.
    val semantics = lookup.lookupCommandPsi(this) ?: return null
    val idx = LArgument.indexOfFirstOptional(semantics.arguments) { it.name == argument }
    if (idx == -1) return null
    return this.optionalParameterText(idx)
}

/**
 * Checks whether the command is known by TeXiFy.
 *
 * @return Whether the command is known (`true`), or unknown (`false`).
 */
fun LatexCommands.isKnown(): Boolean = AllPredefined.lookupCommandPsi(this) != null

/**
 * Finds the indentation of the line where the section command starts.
 */
fun LatexCommands.findIndentation(): String {
    val file = containingFile
    val document = file.document() ?: return ""
    val lineNumber = document.getLineNumber(textOffset)
    return document.lineIndentation(lineNumber)
}

/**
 * Returns the forced first required parameter of a command as a command.
 *
 * This allows both example constructs `\\usepackage{\\foo}}` and `\\usepackage\\foo`,
 * which are equivalent.
 * Note that when the command does not take parameters this method might return untrue results.
 *
 * @return The forced first required parameter of the command.
 */
fun LatexCommands.forcedFirstRequiredParameterAsCommand(): LatexCommands? {
    val parameters = requiredParameters()
    if (parameters.isNotEmpty()) {
        val parameter = parameters.first()
        val found = PsiTreeUtil.findChildrenOfType(parameter, LatexCommands::class.java)
        return if (found.size == 1) found.first() else null
    }

    // This is just a bit of guesswork about the parser structure.
    // Probably, if we're looking at a \def\mycommand, if the sibling isn't it, probably the parent has a sibling.
    return nextSibling?.nextSiblingOfType(LatexCommands::class) ?: parent?.nextSiblingIgnoreWhitespace()?.findFirstChildOfType(LatexCommands::class)
}