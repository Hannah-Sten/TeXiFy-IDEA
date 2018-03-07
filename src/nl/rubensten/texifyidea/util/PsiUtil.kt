package nl.rubensten.texifyidea.util

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import nl.rubensten.texifyidea.index.BibtexIdIndex
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.index.LatexDefinitionIndex
import nl.rubensten.texifyidea.index.LatexIncludesIndex
import nl.rubensten.texifyidea.lang.DefaultEnvironment
import nl.rubensten.texifyidea.lang.Environment
import nl.rubensten.texifyidea.psi.*
import kotlin.reflect.KClass

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// PSI ELEMENT ///////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Get the offset where the psi element ends.
 */
fun PsiElement.endOffset(): Int = textOffset + textLength

/**
 * @see [PsiTreeUtil.getChildrenOfType]
 */
fun <T : PsiElement> PsiElement.childrenOfType(clazz: KClass<T>): Collection<T> {
    return PsiTreeUtil.findChildrenOfType(this, clazz.java)
}

/**
 * @see [PsiTreeUtil.getChildrenOfType]
 */
inline fun <reified T : PsiElement> PsiElement.childrenOfType(): Collection<T> = childrenOfType(T::class)

/**
 * Finds the fierst element that matches a given predicate.
 */
@Suppress("UNCHECKED_CAST")
fun <T : PsiElement> PsiElement.findFirstChild(predicate: (PsiElement) -> Boolean): T? {
    for (child in children) {
        if (predicate(this)) {
            return child as? T
        }

        val first = child.findFirstChild<T>(predicate)
        if (first != null) {
            return first
        }
    }

    return null
}

/**
 * Finds the first child of a certain type.
 */
@Suppress("UNCHECKED_CAST")
fun <T : PsiElement> PsiElement.firstChildOfType(clazz: KClass<T>): T? {
    for (child in this.children) {
        if (clazz.java.isAssignableFrom(child.javaClass)) {
            return child as? T
        }

        val first = child.firstChildOfType(clazz)
        if (first != null) {
            return first
        }
    }

    return null
}

/**
 * Finds the last child of a certain type.
 */
@Suppress("UNCHECKED_CAST")
fun <T : PsiElement> PsiElement.lastChildOfType(clazz: KClass<T>): T? {
    for (child in children.reversedArray()) {
        if (clazz.java.isAssignableFrom(child.javaClass)) {
            return child as? T
        }

        val last = child.lastChildOfType(clazz)
        if (last != null) {
            return last
        }
    }

    return null
}

/**
 * @see [PsiTreeUtil.getParentOfType]
 */
fun <T : PsiElement> PsiElement.parentOfType(clazz: KClass<T>): T? = PsiTreeUtil.getParentOfType(this, clazz.java)

/**
 * Checks if the psi element has a parent of a given class.
 */
fun <T : PsiElement> PsiElement.hasParent(clazz: KClass<T>): Boolean = parentOfType(clazz) != null

/**
 * Checks if the psi element is in math mode or not.
 *
 * @return `true` when the element is in math mode, `false` when the element is in no math mode.
 */
fun PsiElement.inMathContext(): Boolean {
    return hasParent(LatexMathContent::class) || hasParent(LatexDisplayMath::class) || inDirectEnvironmentContext(Environment.Context.MATH)
}

/**
 * Check if the element is in a comment or not.
 */
fun PsiElement.inComment() = inDirectEnvironmentContext(Environment.Context.COMMENT) || when (this) {
    is PsiComment -> true
    else -> this is LeafPsiElement && elementType == LatexTypes.COMMAND_TOKEN
}

/**
 * @see LatexPsiUtil.getPreviousSiblingIgnoreWhitespace
 */
fun PsiElement.previousSiblingIgnoreWhitespace() = LatexPsiUtil.getPreviousSiblingIgnoreWhitespace(this)

/**
 * @see LatexPsiUtil.getNextSiblingIgnoreWhitespace
 */
fun PsiElement.nextSiblingIgnoreWhitespace() = LatexPsiUtil.getNextSiblingIgnoreWhitespace(this)

/**
 * Finds the next sibling of the element that has the given type.
 *
 * @return The first following sibling of the given type, or `null` when the sibling couldn't be found.
 */
fun <T : PsiElement> PsiElement.nextSiblingOfType(clazz: KClass<T>): T? {
    var sibling: PsiElement? = this
    while (sibling != null) {
        if (clazz.java.isAssignableFrom(sibling::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return sibling as T
        }

        sibling = sibling.nextSibling
    }

    return null
}

/**
 * Finds the previous sibling of the element that has the given type.
 *
 * @return The first previous sibling of the given type, or `null` when the sibling couldn't be found.
 */
fun <T : PsiElement> PsiElement.previousSiblingOfType(clazz: KClass<T>): T? {
    var sibling: PsiElement? = this
    while (sibling != null) {
        if (clazz.java.isAssignableFrom(sibling::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return sibling as T
        }

        sibling = sibling.prevSibling
    }

    return null
}

/**
 * @see LatexPsiUtil.getAllChildren
 */
fun PsiElement.allChildren(): List<PsiElement> = LatexPsiUtil.getAllChildren(this)

/**
 * @see LatexPsiUtil.getChildren
 */
fun PsiElement.allLatexChildren(): List<PsiElement> = LatexPsiUtil.getChildren(this)

/**
 * Finds the `generations`th parent of the psi element.
 */
fun PsiElement.grandparent(generations: Int): PsiElement? {
    var parent: PsiElement = this
    for (i in 1..generations) {
        parent = parent.parent ?: return null
    }
    return parent
}

/**
 * Checks if the psi element has a (grand) parent that matches the given predicate.
 */
inline fun PsiElement.hasParentMatching(maxDepth: Int, predicate: (PsiElement) -> Boolean): Boolean {
    var count = 0
    var parent = this.parent
    while (parent != null && parent !is PsiFile) {
        if (predicate(parent)) {
            return true
        }

        parent = parent.parent

        if (count++ > maxDepth) {
            return false
        }
    }

    return false
}

/**
 * Checks whether the psi element is part of a comment or not.
 */
fun PsiElement.isComment(): Boolean {
    return this is PsiComment || inDirectEnvironmentContext(Environment.Context.COMMENT)
}

/**
 * Checks if the element is in a direct environment.
 *
 * This method does not take nested environments into account. Meaning that only the first parent environment counts.
 */
fun PsiElement.inDirectEnvironment(environmentName: String): Boolean = inDirectEnvironment(listOf(environmentName))

/**
 * Checks if the element is one of certain direct environments.
 *
 * This method does not take nested environments into account. Meaning that only the first parent environment counts.
 */
fun PsiElement.inDirectEnvironment(validNames: Collection<String>): Boolean {
    val environment = parentOfType(LatexEnvironment::class) ?: return false
    val nameText = environment.name() ?: return false
    return validNames.contains(nameText.text)
}

/**
 * Runs the given predicate on the direct environment element of this psi element.
 *
 * @return `true` when the predicate tests `true`, or `false` when there is no direct environment or when the
 *              predicate failed.
 */
inline fun PsiElement.inDirectEnvironmentMatching(predicate: (LatexEnvironment) -> Boolean): Boolean {
    val environment = parentOfType(LatexEnvironment::class) ?: return false
    return predicate(environment)
}

/**
 * Checks if the psi element is a child of `parent`.
 *
 * @return `true` when the element is a child of `parent`, `false` when the element is not a child of `parent` or when
 *          `parent` is `null`
 */
fun PsiElement.isChildOf(parent: PsiElement?): Boolean {
    if (parent == null) {
        return false
    }

    return hasParentMatching(1000) { it == parent }
}

/**
 * Checks if the psi element has a direct environment with the given context.
 */
fun PsiElement.inDirectEnvironmentContext(context: Environment.Context): Boolean {
    val environment = parentOfType(LatexEnvironment::class) ?: return context == Environment.Context.NORMAL
    return inDirectEnvironmentMatching {
        DefaultEnvironment.fromPsi(environment)?.context == context
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// PSI FILE //////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Get the corresponding document of the PsiFile.
 */
fun PsiFile.document(): Document? = PsiDocumentManager.getInstance(project).getDocument(this)

/**
 * @see [LatexCommandsIndex.getIndexedCommands]
 */
fun PsiFile.commandsInFile(): Collection<LatexCommands> = LatexCommandsIndex.getItems(this)

/**
 * @see [LatexCommandsIndex.getIndexedCommandsInFileSet]
 */
fun PsiFile.commandsInFileSet(): Collection<LatexCommands> = LatexCommandsIndex.getItemsInFileSet(this)

/**
 * @see [BibtexIdIndex.getIndexedIdsInFileSet]
 */
fun PsiFile.bibtexIdsInFileSet() = BibtexIdIndex.getIndexedIdsInFileSet(this)

/**
 * @see TexifyUtil.getFileRelativeTo
 */
fun PsiFile.fileRelativeTo(path: String, extensions: Set<String>? = null): PsiFile? = TexifyUtil.getFileRelativeTo(this, path, extensions)

/**
 * @see TexifyUtil.findLabelsInFileSet
 */
fun PsiFile.labelsInFileSet(): Set<String> = TexifyUtil.findLabelsInFileSet(this)

/**
 * @see TexifyUtil.getReferencedFileSet
 */
fun PsiFile.referencedFileSet(): Set<PsiFile> = TexifyUtil.getReferencedFileSet(this)

/**
 * Get the editor of the file if it is currently opened.
 */
fun PsiFile.openedEditor() = FileEditorManager.getInstance(project).selectedTextEditor

/**
 * Get all the definitions in the file.
 */
fun PsiFile.definitions(): Collection<LatexCommands> {
    return LatexDefinitionIndex.getItems(this)
            .filter { it.isDefinition() }
}

/**
 * Get all the definitions and redefinitions in the file.
 */
fun PsiFile.definitionsAndRedefinitions(): Collection<LatexCommands> {
    return LatexDefinitionIndex.getItems(this)
}

/**
 * Get all the definitions in the file set.
 */
fun PsiFile.definitionsInFileSet(): Collection<LatexCommands> {
    return LatexDefinitionIndex.getItemsInFileSet(this)
            .filter { it.isDefinition() }
}

/**
 * Get all the definitions and redefinitions in the file set.
 */
fun PsiFile.definitionsAndRedefinitionsInFileSet(): Collection<LatexCommands> {
    return LatexDefinitionIndex.getItemsInFileSet(this)
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// LATEX ELEMENTS ////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Checks whether the given LaTeX commands is a (re)definition or not.
 *
 * This is either a command definition or an environment (re)definition.
 *
 * @return `true` if the command is an environment (re)definition or a command (re)definition, `false` when the command is
 *         `null` or otherwise.
 */
fun LatexCommands?.isDefinitionOrRedefinition() = this != null && (DEFINITIONS.contains(this.name) || REDEFINITIONS.contains(this.name))

/**
 * Checks whether the given LaTeX commands is a definition or not.
 *
 * This is either a command definition or an environment definition. Does not count redefinitions.
 *
 * @return `true` if the command is an environment definition or a command definition, `false` when the command is
 *         `null` or otherwise.
 */
fun LatexCommands?.isDefinition() = this != null && DEFINITIONS.contains(this.name)

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
 * @see TexifyUtil.getForcedFirstRequiredParameterAsCommand
 */
fun LatexCommands.firstRequiredParamAsCommand(): LatexCommands? = TexifyUtil.getForcedFirstRequiredParameterAsCommand(this)

/**
 * Get the command that gets defined by a definition (`\let` or `\def` command).
 */
fun LatexCommands.definitionCommand(): LatexCommands? = nextCommand()

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
 * @see TexifyUtil.getForcedFirstRequiredParameterAsCommand
 */
fun LatexCommands.forcedFirstRequiredParameterAsCommand(): LatexCommands? = TexifyUtil.getForcedFirstRequiredParameterAsCommand(this)

/**
 * Get the name of the command that is defined by `this` command.
 */
fun LatexCommands.definedCommandName() = when (name) {
    "\\DeclareMathOperator", "\\newcommand" -> forcedFirstRequiredParameterAsCommand()?.name
    else -> definitionCommand()?.name
}

/**
 * @see TexifyUtil.isCommandKnown
 */
fun LatexCommands.isKnown(): Boolean = TexifyUtil.isCommandKnown(this)

/**
 * Get the environment name of a begin/end command.
 *
 * @param element
 *              Either a [LatexBeginCommand] or a [LatexEndCommand]
 */
private fun beginOrEndEnvironmentName(element: PsiElement) = element.firstChildOfType(LatexNormalText::class)?.text

/**
 * Get the `index+1`th required parameter of the command.
 *
 * @throws IllegalArgumentException When the index is negative.
 */
@Throws(IllegalArgumentException::class)
fun LatexCommands.requiredParameter(index: Int): String? {
    require(index >= 0, { "Index must not be negative" })

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
 * @see TexifyUtil.isEntryPoint
 */
fun LatexBeginCommand.isEntryPoint(): Boolean = TexifyUtil.isEntryPoint(this)

/**
 * Looks up the name of the environment in the required parameter.
 */
fun LatexEnvironment.name(): LatexNormalText? {
    return firstChildOfType(LatexNormalText::class)
}

/**
 * Checks if the environment contains the given context.
 */
fun LatexEnvironment.isContext(context: Environment.Context): Boolean {
    val name = name()?.text ?: return false
    val environment = Environment[name] ?: return false
    return environment.context == context
}

/**
 * Get the environment name of the begin command.
 */
fun LatexBeginCommand.environmentName(): String? = beginOrEndEnvironmentName(this)

/**
 * Finds the [LatexEndCommand] that matches the begin command.
 */
fun LatexBeginCommand.endCommand(): LatexEndCommand? = nextSiblingOfType(LatexEndCommand::class)

/**
 * Get the environment name of the end command.
 */
fun LatexEndCommand.environmentName(): String? = beginOrEndEnvironmentName(this)

/**
 * Finds the [LatexBeginCommand] that matches the end command.
 */
fun LatexEndCommand.beginCommand(): LatexBeginCommand? = previousSiblingOfType(LatexBeginCommand::class)

/**
 * Checks if the latex content objects is a display math environment.
 */
fun LatexContent.isDisplayMath() = firstChildOfType(LatexDisplayMath::class) != null && firstChildOfType(LatexEnvironment::class) == null


/**
 * Checks if the fileset for this file has a bibliography included.
 *
 * @return `true` when the fileset has a bibliography included, `false` otherwise.
 */
fun PsiFile.hasBibliography() = LatexIncludesIndex.getItemsInFileSet(this).any { it.name == "\\bibliography" }
