package nl.hannahsten.texifyidea.util.parser

import com.intellij.codeInsight.PsiEquivalenceUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.psi.*

/**
 * Checks if the environment contains the given context.
 */
fun LatexEnvironment.isContext(context: Environment.Context): Boolean {
    val name = getEnvironmentName()
    val environment = Environment[name] ?: return false
    return environment.context == context
}

/**
 * Finds the [LatexEndCommand] that matches the begin command.
 */
fun LatexBeginCommand.endCommand(): LatexEndCommand? = nextSiblingOfType(LatexEndCommand::class)

/**
 * Gets the required parameters of the `\begin` command, not including the environment name.
 *
 * For example, for `\begin{env}{1}{2}`, this will return a list of `{1}, {2}`.
 *
 * @return A list of all required parameters.
 */
fun LatexBeginCommand.requiredParameters(): List<LatexRequiredParam> {
    return this.parameterList.mapNotNull { it.requiredParam }
}

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
    return this.environmentName() == DefaultEnvironment.DOCUMENT.environmentName
}

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
fun PsiFile.hasBibliography(): Boolean {
    return NewCommandsIndex.getByNameInFileSet("\\bibliography", this).isNotEmpty()
}

/**
 * Checks if the fileset for this file uses \printbibliography, in which case the user probably wants to use biber.
 *
 * @return `true` when the fileset has a bibliography included, `false` otherwise.
 */
fun PsiFile.usesBiber() = NewCommandsIndex.getByNameInFileSet("\\printbibliography", this).isNotEmpty()

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
    return this.collectSubtreeTo(mutableSetOf(), Int.MAX_VALUE) { e ->
        val dependency = when (e) {
            is LatexCommands -> {
                // If the command is a known command, add its dependency.
                LatexCommand.lookupInAll(e)?.firstOrNull()?.dependency
            }
            is LatexEnvironment -> {
                // If the environment is a known environment, add its dependency.
                Environment.lookup(e.getEnvironmentName())?.dependency
            }
            else -> null
        }
        dependency?.takeIf { it.isDefault.not() }
    }
}