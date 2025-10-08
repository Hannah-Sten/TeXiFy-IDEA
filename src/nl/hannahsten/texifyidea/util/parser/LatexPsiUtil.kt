package nl.hannahsten.texifyidea.util.parser

import com.intellij.codeInsight.PsiEquivalenceUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub
import nl.hannahsten.texifyidea.index.stub.LatexParameterStub
import nl.hannahsten.texifyidea.index.stub.requiredParamAt
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LArgumentType
import nl.hannahsten.texifyidea.lang.LatexContextIntro
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexContext
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.LatexSemanticsCommandLookup
import nl.hannahsten.texifyidea.lang.LatexSemanticsEnvLookup
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.lang.predefined.AllPredefined
import nl.hannahsten.texifyidea.lang.predefined.EnvironmentNames
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * Finds the [LatexEndCommand] that matches the begin command.
 */
fun LatexBeginCommand.endCommand(): LatexEndCommand? = nextSiblingOfType(LatexEndCommand::class)

/**
 * Checks if the given latex command marks a valid entry point for latex compilation.
 *
 * A valid entry point means that a latex compilation can start from the file containing the
 * given command.
 *
 * @return `true` if the command marks a valid entry point, `false` if not.
 */
fun LatexBeginCommand.isEntryPoint(): Boolean {
    // Currently: only allowing `\\begin{document}`.
    return this.environmentName() == EnvironmentNames.DOCUMENT
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

fun PsiElement.findDependencies(): Set<LatexLib> {
    return this.collectSubtreeTo(mutableSetOf(), Int.MAX_VALUE) { e ->
        val dependency = when (e) {
            is LatexCommands -> {
                // If the command is a known command, add its dependency.
                val nameNoSlash = e.name?.removePrefix("\\") ?: ""
                AllPredefined.lookupCommand(nameNoSlash)?.dependency
            }

            is LatexEnvironment -> {
                // If the environment is a known environment, add its dependency.
                AllPredefined.lookupEnv(e.getEnvironmentName())?.dependency
            }

            else -> null
        }
        dependency?.takeIf { it.requiresImport }
    }
}

fun LatexSemanticsEnvLookup.lookupEnv(name: String?): LSemanticEnv? {
    if (name == null) return null
    return lookupEnv(name)
}

fun LatexSemanticsCommandLookup.lookupCommandN(name: String?): LSemanticCommand? {
    if (name == null) return null
    return lookupCommand(name)
}

fun LatexSemanticsCommandLookup.lookupCommandPsi(cmd: LatexCommands): LSemanticCommand? {
    return lookupCommandN(cmd.nameWithoutSlash)
}

/**
 * Utility functions for the Latex PSI file structure.
 */
object LatexPsiUtil {

    /**
     * Get the name of the command that is defined by a definition command stub.
     */
    fun getDefinedCommandName(defStub: LatexCommandsStub): String? {
        defStub.requiredParamAt(0)?.let {
            return it.trim()
        }
        // \def\cmd\something
        // find the next command after it, which can be a bit slow, but I don't know a better way
        val children = defStub.parentStub!!.childrenStubs
        val siblingIndex = children.indexOfFirst { it === defStub } + 1
        if (siblingIndex < 0 || siblingIndex >= children.size) return null
        val sibling = children[siblingIndex] as? LatexCommandsStub ?: return null
        // let us not treat \let\cmd\relax as a definition
        val nextSibling = children.getOrNull(siblingIndex + 1)
        if (nextSibling is LatexCommandsStub && nextSibling.commandToken == "\\relax") {
            return null
        }
        return sibling.commandToken
    }

    fun getDefinedCommandName(defCommand: LatexCommands): String? {
        val defStub = defCommand.stub
        if (defStub != null) {
            return getDefinedCommandName(defStub)
        }
        // we use the PSI tree now, since the operation of finding the next command seems to be expensive with stubs
        if (defCommand.parameterList.isNotEmpty()) {
            return defCommand.parameterList[0].findFirstChildTyped<LatexCommands>()?.name
        }
        // \def\cmd\something
        val nextCommand = defCommand.nextContextualSibling { it is LatexCommands } as? LatexCommands ?: return null
        return nextCommand.name
    }

    fun getDefinedCommandElement(cmd: LatexCommands): LatexCommands? {
        cmd.firstRequiredParameter()?.let {
            return it.findFirstChildTyped<LatexCommands>()
        }
        return cmd.nextContextualSibling { true } as? LatexCommands
    }

    /**
     * Check if the command is inside a definition command as a parameter, like `\newcommand{\cmd}{}`.
     */
    private fun isInsideNewCommandDef(cmd: LatexComposite): Boolean {
        // command - parameter - required_parameter - required_param_content - parameter_text - command
        //                                                                                    - NormalTextWord
        // we leave some space
        val parentParameter = cmd.firstParentOfType<LatexParameter>(5) ?: return false
        val defCommand = parentParameter.firstParentOfType<LatexCommands>(1) ?: return false
        val name = defCommand.name
        if (name !in CommandMagic.definitions) return false
        return defCommand.firstParameter() === parentParameter // they should be exactly the same object
    }

    private fun isInsidePlainDef(cmd: PsiElement): Boolean {
        // \def\cmd\something
        val prevCmd = cmd.prevContextualSibling { it is LatexCommands } as? LatexCommands ?: return false
        if (prevCmd.name !in CommandMagic.definitions) return false
        if (prevCmd.hasRequiredParameter()) return false
        return true
    }

    fun stubTypeToLArgumentType(type: Int): LArgumentType {
        return when (type) {
            LatexParameterStub.REQUIRED -> LArgumentType.REQUIRED
            LatexParameterStub.OPTIONAL -> LArgumentType.OPTIONAL
            else -> LArgumentType.REQUIRED
        }
    }

    fun parameterToLArgumentType(parameter: LatexParameter): LArgumentType {
        return when {
            parameter.requiredParam != null -> LArgumentType.REQUIRED
            parameter.optionalParam != null -> LArgumentType.OPTIONAL
            else -> LArgumentType.REQUIRED // default to required if no parameters are present
        }
    }

    fun parameterTypeMatchesStub(parameter: LatexParameterStub, type: LArgumentType): Boolean {
        return when (type) {
            LArgumentType.REQUIRED -> parameter.type == LatexParameterStub.REQUIRED
            LArgumentType.OPTIONAL -> parameter.type == LatexParameterStub.OPTIONAL
        }
    }

    inline fun processArgumentsWithSemantics(cmd: LatexCommandWithParams, semantics: LSemanticCommand, action: (LatexParameter, LArgument?) -> Unit) {
        processArgumentsWithSemantics(cmd, semantics.arguments, action)
    }

    inline fun processArgumentsWithNonNullSemantics(cmd: LatexCommandWithParams, semantics: LSemanticCommand, action: (LatexParameter, LArgument) -> Unit) {
        processArgumentsWithSemantics(cmd, semantics.arguments) { param, arg ->
            if(arg != null) action(param, arg)
        }
    }

    inline fun processArgumentsWithSemantics(cmd: LatexCommandWithParams, argList: List<LArgument>, action: (LatexParameter, LArgument?) -> Unit) {
        var argIdx = 0
        val argSize = argList.size
        cmd.forEachParameter forEach@{ param ->
            if (argIdx >= argSize) {
                action(param, null) // no suitable arguments
                return@forEach
            }
            var arg: LArgument? = null
            val paramType = parameterToLArgumentType(param)
            when (paramType) {
                LArgumentType.REQUIRED -> while (argIdx < argSize) {
                    // find the next required argument
                    val argument = argList[argIdx++]
                    if (argument.type == LArgumentType.REQUIRED) {
                        arg = argument
                        break
                    }
                }

                LArgumentType.OPTIONAL -> {
                    val argument = argList[argIdx]
                    if (argument.type == LArgumentType.OPTIONAL) {
                        arg = argument
                        argIdx++
                    }
                    // just skip if the next argument is not optional
                }
            }
            action(param, arg)
        }
    }

    inline fun processArgumentsWithSemantics(cmd: LatexCommandsStub, semantics: LSemanticCommand, action: (LatexParameterStub, LArgument) -> Unit) {
        val arguments = semantics.arguments
        var argIdx = 0
        val argSize = arguments.size
        cmd.parameters.forEach { param ->
            if (argIdx >= argSize) return
            while (true) {
                val argument = arguments[argIdx]
                if (parameterTypeMatchesStub(param, argument.type)) {
                    action(param, argument)
                    argIdx++
                    return@forEach // continue to next parameter
                }
                else if (argument.type == LArgumentType.OPTIONAL) {
                    argIdx++ // skip optional arguments that are not present
                }
            }
        }
    }

    fun alignCommandArgument(command: LatexCommandWithParams, parameter: LatexParameter, arguments: List<LArgument>): LArgument? {
        val command = parameter.firstParentOfType<LatexCommands>() ?: return null
        processArgumentsWithSemantics(command, arguments) { p, arg ->
            if (p == parameter) return arg
        }
        return null
    }

    private fun resolveBeginCommandContext(parameter: LatexParameter, lookup: LatexSemanticsLookup): LatexContextIntro? {
        val beginCommand = parameter.firstParentOfType<LatexBeginCommand>(3) ?: return null
        val name = beginCommand.environmentName() ?: return null
        val semantics = lookup.lookupEnv(name) ?: return null
        val arg = alignCommandArgument(beginCommand, parameter, semantics.arguments) ?: return null
        return arg.contextSignature
    }

    private fun resolveCommandParameterContext(parameter: LatexParameter, lookup: LatexSemanticsLookup): LatexContextIntro? {
        val command = parameter.firstParentOfType<LatexCommands>(3) ?: return resolveBeginCommandContext(parameter, lookup)
        val name = command.name?.removePrefix("\\") ?: return null
        val semantics = lookup.lookupCommand(name) ?: return null
        val arg = alignCommandArgument(command, parameter, semantics.arguments) ?: return null
        return arg.contextSignature
    }

    private fun resolveEnvironmentContext(env: LatexEnvironment, lookup: LatexSemanticsLookup): LatexContextIntro? {
        val name = env.getEnvironmentName()
        val semantics = lookup.lookupEnv(name) ?: return null
        return semantics.contextSignature
    }

    fun resolveContextUpward(e: PsiElement): LContextSet {
        val file = e.containingFile ?: return emptySet()
        val lookup = LatexDefinitionService.getInstance(file.project).getDefBundlesMerged(file)
        return resolveContextUpward(e, lookup)
    }

    /**
     * Resolve the context introductions at the given element by traversing the PSI tree upwards and collecting context changes.
     * The list is ordered from innermost to outermost context introduction.
     *
     * @see LatexWithContextStateTraverser
     */
    fun resolveContextIntroUpward(e: PsiElement, lookup: LatexSemanticsLookup, shortCircuit: Boolean = false): List<LatexContextIntro> {
        var collectedContextIntro: MutableList<LatexContextIntro>? = null
        var current: PsiElement = e
        // see Latex.bnf
        while (true) {
            current = current.firstStrictParent { // `firstParent` is inclusive
                it is LatexParameter || it is LatexEnvironment || it is LatexMathEnvironment
            } ?: break
            val intro = when (current) {
                is LatexParameter -> resolveCommandParameterContext(current, lookup) ?: continue
                is LatexEnvironment -> resolveEnvironmentContext(current, lookup) ?: continue
                is LatexInlineMath -> LatexContextIntro.INLINE_MATH
                is LatexMathEnvironment -> LatexContextIntro.MATH
                else -> continue
            }
            if (shortCircuit) {
                if (intro is LatexContextIntro.Assign || intro is LatexContextIntro.Clear) {
                    if (collectedContextIntro == null) return listOf(intro)
                    collectedContextIntro.add(intro)
                    return collectedContextIntro
                }
            }
            if (intro is LatexContextIntro.Inherit) continue
            if (collectedContextIntro == null) collectedContextIntro = mutableListOf()
            collectedContextIntro.add(intro)
        }
        return collectedContextIntro ?: emptyList()
    }

    /**
     * Resolve the context at the given element by traversing the PSI tree upwards and collecting context changes.
     * If no context changes are found, the [baseContext] is returned.
     */
    fun resolveContextUpward(e: PsiElement, lookup: LatexSemanticsLookup, baseContext: LContextSet = LatexContexts.baseContexts): LContextSet {
        val list = resolveContextIntroUpward(e, lookup, shortCircuit = true)
        if (list.isEmpty()) return baseContext
        return LatexContextIntro.buildContextReversedList(list, baseContext)
    }

    fun isInsideContext(e: PsiElement, context: LatexContext, lookup: LatexSemanticsLookup, baseContext: LContextSet = LatexContexts.baseContexts): Boolean {
        val currentContext = resolveContextUpward(e, lookup, baseContext)
        return context in currentContext
    }

    /**
     * Check if the given element is nested inside a command definition (maybe deeply).
     */
    fun isInsideDefinition(e: PsiElement, lookup: LatexSemanticsLookup): Boolean {
        // unfortunately, the context does not contain enough information to determine whether we are inside a command definition as they can be overridden
        // For example, in `\newcommand{\myref}[1]{$ \text{\ref{#1}} $}`,  `#1` only has the context, while `\ref` has context `<text>`.
        // To improve it, we have to introduce penetrating context or conflicting context, making our context system more complex.
        // When we find more use cases requiring advanced context system later, we can upgrade it to fit the needs
        if(isInsidePlainDef(e)) {
            return true
        }
        val introList = resolveContextIntroUpward(e, lookup, shortCircuit = false)
        return introList.any { it.introduces(LatexContexts.InsideDefinition) }
    }

    /**
     * Check if the given element is nested inside a command definition (maybe deeply).
     */
    fun isInsideDefinition(element: PsiElement): Boolean {
        val bundle = LatexDefinitionService.getBundleFor(element)
        return isInsideDefinition(element, bundle)
    }

    /**
     * Check if the given element is the command being defined, e.g. `\hi` in case of `\newcommand{\hi}{}`.
     */
    fun isCommandBeingDefined(element: LatexCommands): Boolean {
        val bundle = LatexDefinitionService.getBundleFor(element)
        return isInsideContext(element, LatexContexts.CommandDeclaration, bundle)
    }

    /**
     * Traverse the given element and all its children, recording context introductions.
     * The action is executed at each element, with the current list of context introductions.
     * The list is ordered from outermost to innermost context introduction.
     *
     * Returns the list of context introductions at the end of the traversal, which may not be empty if unmatched context introductions are present,
     * for example when traversing a `\begin` command without the matching `\end` command.
     *
     */
    fun traverseRecordingContextIntro(
        e: PsiElement,
        lookup: LatexSemanticsLookup,
        action: (PsiElement, List<LatexContextIntro>) -> Unit
    ): List<LatexContextIntro> {
        val visitor = RecordingContextIntroTraverserWithAction(lookup, action)
        visitor.traverse(e)
        return visitor.exitState
    }

    private class RecordingContextIntroTraverserWithAction(
        lookup: LatexSemanticsLookup,
        private val action: (PsiElement, List<LatexContextIntro>) -> Unit
    ) : RecordingContextIntroTraverser(lookup) {
        override fun elementStart(e: PsiElement): WalkAction {
            action(e, state)
            return super.elementStart(e)
        }
    }

    /**
     * Check if the given environment introduces the given context.
     */
    fun isContextIntroduced(element: LatexEnvironment, lookup: LatexSemanticsLookup, context: LatexContext): Boolean {
        val name = element.getEnvironmentName()
        val semantics = lookup.lookupEnv(name) ?: return false
        val signature = semantics.contextSignature
        return signature.introduces(context)
    }
}