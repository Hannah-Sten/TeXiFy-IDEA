package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexContextIntro
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.parser.forEachDirectChild

/**
 * A traverser that keeps track of the current context, updating it when entering math mode or
 * when entering commands/environments with a specific context intro.
 *
 * The state is updated by applying the [LatexContextIntro] to the current state.
 *
 * @param S The type of the state being tracked.
 *
 * @author Li Ernest
 */
abstract class LatexWithContextStateTraverser<S>(
    initialState: S,
    protected val lookup: LatexSemanticsLookup
) {

    protected enum class WalkAction {
        CONTINUE,

        /**
         * Skip the children of the current element, but continue with the next sibling/parent.
         */
        SKIP_CHILDREN,

        /**
         * Stop the whole traversal.
         */
        STOP_WALK;

        val shouldContinue: Boolean get() = this != STOP_WALK
    }

    /**
     * Current state of the traverser.
     * It can be updated when entering/exiting contexts.
     */
    protected var state: S = initialState

    /**
     * Called when starting to process an element.
     */
    protected open fun elementStart(e: PsiElement): WalkAction = WalkAction.CONTINUE

    /**
     * Called when finishing processing an element.
     *
     * @return Whether to continue the whole traversal.
     */
    protected open fun elementFinish(e: PsiElement): Boolean = true

    /**
     * Update the [state] when entering a context intro if returns [WalkAction.CONTINUE],
     * or skip/stop the traversal if returns [WalkAction.SKIP_CHILDREN] or [WalkAction.STOP_WALK].
     * Note that for the latter two cases, the state should not be changed.
     *
     *
     */
    protected abstract fun enterContextIntro(intro: LatexContextIntro): WalkAction

    /**
     * Restore the state when exiting a context intro.
     *
     * @param old The old state recorded before entering the context intro.
     * @param intro The context intro that is being exited.
     */
    protected open fun exitContextIntro(old: S, intro: LatexContextIntro) {
        // Default implementation does nothing.
    }

    private fun enterElementWithIntro(e: PsiElement, intro: LatexContextIntro): Boolean {
        val oldState = state
        val action = enterContextIntro(intro)
        if (action != WalkAction.CONTINUE) {
            return action.shouldContinue
        }
        val cont = traverseRecur(e)
        exitContextIntro(oldState, intro)
        return cont
    }

    protected fun traverseCommandRecur(element: LatexCommandWithParams, args: List<LArgument>): Boolean {
        LatexPsiUtil.processArgumentsWithSemantics(element, args) { parameter, argument ->
            val cont = enterElementWithIntro(parameter, argument?.contextSignature ?: LatexContextIntro.Inherit)
            if (!cont) return false
        }
        return elementFinish(element)
    }

    protected fun traverseEnvironmentRecur(element: LatexEnvironment, semantics: LSemanticEnv): Boolean {
        if (!traverseCommandRecur(element.beginCommand, semantics.arguments)) return false
        element.environmentContent?.let {
            val ret = enterElementWithIntro(it, semantics.contextSignature)
            if (!ret) return false
        }
        return elementFinish(element)
    }

    private fun traverseChildren(e: PsiElement): Boolean {
        e.forEachDirectChild {
            if (!traverseRecur(it)) {
                return false
            }
        }
        return true
    }

    /**
     *
     * @see LatexPsiUtil.resolveContextIntroUpward
     */
    protected fun traverseRecur(e: PsiElement): Boolean {
        val action = elementStart(e)
        when (action) {
            WalkAction.STOP_WALK -> return false
            WalkAction.SKIP_CHILDREN -> return true
            else -> {
                /* CONTINUE */
            }
        }

        var currentIntro: LatexContextIntro? = null

        when (e) {
            is LatexCommands -> {
                val semantic = lookup.lookupCommand(e.name?.removePrefix("\\") ?: "")
                if (semantic != null) {
                    return traverseCommandRecur(e, semantic.arguments)
                }
            }

            is LatexEnvironment -> {
                val semantic = lookup.lookupEnv(e.getEnvironmentName())
                if (semantic != null) {
                    return traverseEnvironmentRecur(e, semantic)
                }
            }

            is LatexInlineMath -> {
                currentIntro = LatexContextIntro.INLINE_MATH
            }

            is LatexMathContent -> {
                currentIntro = LatexContextIntro.MATH
            }
        }

        val ret = if (currentIntro != null) {
            val oldState = state
            val actionIntro = enterContextIntro(currentIntro)
            if (actionIntro != WalkAction.CONTINUE) {
                return actionIntro.shouldContinue
            }
            val ret = traverseChildren(e)
            exitContextIntro(oldState, currentIntro)
            ret
        }
        else {
            val ret = traverseChildren(e)
            ret
        }
        return ret && elementFinish(e)
    }
}

/**
 * A traverser that records all context introductions that are currently active.
 *
 */
abstract class RecordingContextIntroTraverser(
    lookup: LatexSemanticsLookup,
) : LatexWithContextStateTraverser<MutableList<LatexContextIntro>>(mutableListOf(), lookup) {
    override fun enterContextIntro(intro: LatexContextIntro): WalkAction {
        state.add(intro)
        return WalkAction.CONTINUE
    }

    override fun exitContextIntro(old: MutableList<LatexContextIntro>, intro: LatexContextIntro) {
        val lastIntro = old.lastOrNull()
        if (lastIntro === intro) old.removeLast() // they should be exactly the same object
    }

    private fun enterBeginEnv(envName: String) {
        val semantics = lookup.lookupEnv(envName) ?: return
        enterContextIntro(semantics.contextSignature)
    }

    private fun exitEndEnv(envName: String) {
        val semantics = lookup.lookupEnv(envName) ?: return
        exitContextIntro(state, semantics.contextSignature)
    }

    override fun elementStart(e: PsiElement): WalkAction {
        if (e is LatexCommands) {
            // special handling for begin/end commands that are not parsed as environments
            val name = e.nameWithSlash
            if (name == "\\begin") e.requiredParameterText(0)?.let { enterBeginEnv(it) }
            else if (name == "\\end") e.requiredParameterText(0)?.let { exitEndEnv(it) }
        }
        return WalkAction.CONTINUE
    }

    fun traverse(e: PsiElement): Boolean = traverseRecur(e)

    val exitState: List<LatexContextIntro>
        get() = state
}

/**
 * A traverser that keeps track of the current LaTeX context.
 *
 * It can be used in context-aware inspections.
 */
abstract class LatexWithContextTraverser(
    initialState: LContextSet, lookup: LatexSemanticsLookup
) : LatexWithContextStateTraverser<LContextSet>(initialState, lookup) {

    override fun enterContextIntro(intro: LatexContextIntro): WalkAction {
        state = intro.applyTo(state)
        return WalkAction.CONTINUE
    }

    override fun exitContextIntro(old: LContextSet, intro: LatexContextIntro) {
        state = old
    }
}