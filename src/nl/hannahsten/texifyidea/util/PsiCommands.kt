package nl.hannahsten.texifyidea.util

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.lang.LatexMathCommand
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.files.document

/**
 * Checks whether the given LaTeX commands is a definition or not.
 *
 * This is either a command definition or an environment definition. Does not count redefinitions.
 *
 * @return `true` if the command is an environment definition or a command definition, `false` when the command is
 *         `null` or otherwise.
 */
fun LatexCommands?.isDefinition() = this != null && this.name in Magic.Command.definitions

/**
 * Checks whether the given LaTeX commands is a (re)definition or not.
 *
 * This is either a command definition or an environment (re)definition.
 *
 * @return `true` if the command is an environment (re)definition or a command (re)definition, `false` when the command is
 *         `null` or otherwise.
 */
fun LatexCommands?.isDefinitionOrRedefinition() = this != null &&
        (this.name in Magic.Command.redefinitions || this.name in Magic.Command.redefinitions)

/**
 * Checks whether the given LaTeX commands is a command definition or not.
 *
 * @return `true` if the command is a command definition, `false` when the command is `null` or otherwise.
 */
fun LatexCommands?.isCommandDefinition(): Boolean {
    return this != null && ("\\newcommand" == name ||
            "\\let" == name ||
            "\\def" == name ||
            "\\DeclareMathOperator" == name ||
            "\\renewcommand" == name)
}

/**
 * Checks whether the given LaTeX commands is an environment definition or not.
 *
 * @return `true` if the command is an environment definition, `false` when the command is `null` or otherwise.
 */
fun LatexCommands?.isEnvironmentDefinition(): Boolean {
    return this != null && ("\\newenvironment" == name ||
            "\\renewenvironment" == name)
}

/**
 * Get the command that gets defined by a definition (`\let` or `\def` command).
 */
fun LatexCommands.definitionCommand(): LatexCommands? = forcedFirstRequiredParameterAsCommand()

/**
 * Checks whether the command has a star or not.
 */
fun LatexCommands.hasStar() = childrenOfType(LeafPsiElement::class).any {
    it.elementType == LatexTypes.STAR
}

/**
 * Looks for the next command relative to the given command.
 *
 * @return The next command in the file, or `null` when there is no such command.
 */
fun LatexCommands.nextCommand(): LatexCommands? {
    val content = parentOfType(LatexContent::class) ?: return null
    val next = content.nextSiblingIgnoreWhitespace() as? LatexContent ?: return null
    return next.firstChildOfType(LatexCommands::class)
}

/**
 * Looks for the previous command relative to the given command.
 *
 * @return The previous command in the file, or `null` when there is no such command.
 */
fun LatexCommands.previousCommand(): LatexCommands? {
    val content = parentOfType(LatexContent::class) ?: return null
    val previous = content.previousSiblingIgnoreWhitespace() as? LatexContent ?: return null
    return previous.firstChildOfType(LatexCommands::class)
}

/**
 * Get the name of the command that is defined by `this` command.
 */
fun LatexCommands.definedCommandName() = when (name) {
    "\\DeclareMathOperator", "\\newcommand" -> forcedFirstRequiredParameterAsCommand()?.name
    else -> definitionCommand()?.name
}

/**
 * Checks whether the command is known by TeXiFy.
 *
 * @return Whether the command is known (`true`), or unknown (`false`).
 */
fun LatexCommands.isKnown(): Boolean {
    val name = name?.substring(1) ?: ""
    return LatexRegularCommand[name] != null || LatexMathCommand[name] != null
}

/**
 * Get the text contents of the `index+1`th required parameter of the command.
 *
 * @throws IllegalArgumentException When the index is negative.
 */
@Throws(IllegalArgumentException::class)
fun LatexCommands.requiredParameter(index: Int): String? {
    require(index >= 0) { "Index must not be negative" }

    val parameters = requiredParameters
    if (parameters.isEmpty() || index >= parameters.size) {
        return null
    }

    return parameters[index]
}

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
 * If the given command is an include command, the contents of the first argument will be read.
 *
 * @return The included filename or `null` when it's not an include command or when there
 * are no required parameters.
 */
fun LatexCommands.includedFileName(): String? {
    if (commandToken.text !in Magic.Command.includes) return null
    val required = requiredParameters
    if (required.isEmpty()) return null
    return required.first()
}

/**
 * Looks up all the required parameters of this command.
 *
 * @return A list of all required parameters.
 */
fun LatexCommands.requiredParameters(): List<LatexRequiredParam> = parameterList.asSequence()
        .filter { it.requiredParam != null }
        .mapNotNull(LatexParameter::getRequiredParam)
        .toList()

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

    val parent = PsiTreeUtil.getParentOfType(this, LatexContent::class.java)
    val sibling = PsiTreeUtil.getNextSiblingOfType(parent, LatexContent::class.java)
    return PsiTreeUtil.findChildOfType(sibling, LatexCommands::class.java)
}

/**
 * Get all [LatexCommands] that are children of the given element.
 */
fun PsiElement.allCommands(): List<LatexCommands> {
    val commands = ArrayList<LatexCommands>()
    allCommands(commands)
    return commands
}

/**
 * Recursive implementation of [allCommands].
 */
private fun PsiElement.allCommands(commands: MutableList<LatexCommands>) {
    forEachChild { it.allCommands(commands) }
    if (this is LatexCommands) {
        commands.add(this)
    }
}