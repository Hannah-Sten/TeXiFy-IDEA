package nl.hannahsten.texifyidea.util.parser

import com.intellij.codeInsight.PsiEquivalenceUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.files.commandsInFileSet

/**
 * Looks up the name of the environment in the required parameter.
 */
fun LatexEnvironment.name(): LatexParameterText? {
    return firstChildOfType(LatexParameterText::class)
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
 * Looks up all the required parameters from this begin command.
 *
 * @return A list of all required parameters.
 */
fun LatexBeginCommand.requiredParameters(): List<LatexRequiredParam> = parameterList.asSequence()
    .filter { it.requiredParam != null }
    .mapNotNull(LatexParameter::getRequiredParam)
    .toList()

/**
 * Checks if the given latex command marks a valid entry point for latex compilation.
 *
 * A valid entry point means that a latex compilation can start from the file containing the
 * given command.
 *
 * @return `true` if the command marks a valid entry point, `false` if not.
 */
fun LatexBeginCommand.isEntryPoint(): Boolean {
    // Currently: only allowing `\begin{document}`.
    val requiredParameters = requiredParameters()
    return requiredParameters.firstOrNull()?.text == "{document}"
}

/**
 * Get the environment name of the end command.
 */
fun LatexEndCommand.environmentName(): String? = beginOrEndEnvironmentName(this)

/**
 * Get the environment name of a begin/end command.
 *
 * @param element
 *              Either a [LatexBeginCommand] or a [LatexEndCommand]
 */
private fun beginOrEndEnvironmentName(element: PsiElement): String? = element.firstChildOfType(LatexParameterText::class)?.text

/**
 * Finds the [LatexBeginCommand] that matches the end command.
 */
fun LatexEndCommand.beginCommand(): LatexBeginCommand? = previousSiblingOfType(LatexBeginCommand::class)

/**
 * Checks if the latex content objects is a display math environment.
 */
fun LatexNoMathContent.isDisplayMath() = children.firstOrNull() is LatexMathEnvironment && children.first().firstChild is LatexDisplayMath

/*
 * Technically it's impossible to determine for all cases whether a users wants to compile with biber or biblatex.
 * But often when people use the biblatex package they use biber.
 * And often, when they use biber they use \printbibliography instead of \bibliography.
 * Hence, the following methods often work - and if they don't, users can easily change the compiler in the run config.
 */

/**
 * Checks if the fileset for this file has a bibliography included.
 *
 * @return `true` when the fileset has a bibliography included, `false` otherwise.
 */
fun PsiFile.hasBibliography() = this.commandsInFileSet().any { it.name == "\\bibliography" }

/**
 * Checks if the fileset for this file uses \printbibliography, in which case the user probably wants to use biber.
 *
 * @return `true` when the fileset has a bibliography included, `false` otherwise.
 */
fun PsiFile.usesBiber() = this.commandsInFileSet().any { it.name == "\\printbibliography" }

/**
 * Looks up the first parent of a given child that has the given class.
 *
 * @param child
 * The child from which to find the parent of.
 * @param parentClass
 * The type the parent has.
 * @return The first parent that has the given class, or `null` when the parent can't be
 * found.
 */
fun <T : PsiElement?> getParentOfType(
    child: PsiElement?,
    parentClass: Class<T>
): T? {
    var element = child
    while (element != null) {
        if (parentClass.isAssignableFrom(element.javaClass)) {
            @Suppress("UNCHECKED_CAST")
            return element as T
        }
        element = element.parent
    }
    return element
}

val LatexParameterText.command: PsiElement?
    get() {
        return this.firstParentOfType(LatexCommands::class)?.firstChild
    }

/**
 * @see PsiElement.findOccurrences(PsiElement)
 */
fun PsiElement.findOccurrences(): List<LatexExtractablePSI> {
    val parent = firstParentOfType(LatexFile::class)
        ?: return emptyList()
    return this.findOccurrences(parent)
}

/**
 * Known weakness: since we will allow the user to extract portions of a text block, this will only extract text when the parent PSI's are identical.
 * However, extracting a single word does not suffer from this as we are extracting an actual token.
 */
fun PsiElement.findOccurrences(searchRoot: PsiElement): List<LatexExtractablePSI> {
    val visitor = object : PsiRecursiveElementVisitor() {
        val foundOccurrences = ArrayList<PsiElement>()
        override fun visitElement(element: PsiElement) {
            if (PsiEquivalenceUtil.areElementsEquivalent(this@findOccurrences, element)) {
                foundOccurrences.add(element)
            }
            else {
                super.visitElement(element)
            }
        }
    }
    searchRoot.acceptChildren(visitor)
    return visitor.foundOccurrences.map { it.asExtractable() }
}

fun PsiElement.findDependencies(): Set<LatexPackage> {
    val commandsDependencies = this.childrenOfType<LatexCommands>()
        .mapNotNull { LatexCommand.lookup(it)?.firstOrNull()?.dependency }
    val environmentDependencies = this.childrenOfType<LatexEnvironment>()
        .mapNotNull { Environment.lookup(it.getEnvironmentName())?.dependency }

    return (commandsDependencies + environmentDependencies).filter { it.isDefault.not() }.toSet()
}