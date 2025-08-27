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
        STOP_WALK
    }

    /**
     * Current state of the traverser.
     * It can be updated when entering/exiting contexts.
     */
    protected var state: S = initialState

    /**
     * Called when starting to process an element.
     */
    protected open fun elementStart(e: PsiElement): WalkAction {
        return WalkAction.CONTINUE
    }

    /**
     * Called when finishing processing an element.
     *
     * @return Whether to continue the whole traversal.
     */
    protected open fun elementFinish(e: PsiElement): Boolean {
        return true
    }

    /**
     * Update the [state] when entering a context intro.
     */
    protected abstract fun enterContextIntro(intro: LatexContextIntro)

    /**
     * Restore the state when exiting a context intro.
     *
     * @param old The old state recorded before entering the context intro.
     * @param intro The context intro that is being exited.
     */
    protected open fun exitContextIntro(old: S, intro: LatexContextIntro) {
        state = old
    }

    protected fun traverseCommandRecur(e: LatexCommandWithParams, args: List<LArgument>): Boolean {
        LatexPsiUtil.processArgumentsWithSemantics(e, args) { parameter, argument ->
            val intro = argument?.contextSignature ?: LatexContextIntro.Inherit
            val oldState = state
            enterContextIntro(intro)
            val action = traverseRecur(parameter)
            exitContextIntro(oldState, intro)
            if (!action) return false
        }
        return elementFinish(e)
    }

    protected fun traverseEnvironmentRecur(e: LatexEnvironment, semantics: LSemanticEnv): Boolean {
        if (!traverseCommandRecur(e.beginCommand, semantics.arguments)) return false
        e.environmentContent?.let {
            val oldState = state
            enterContextIntro(semantics.contextSignature)
            val ret = traverseRecur(it)
            exitContextIntro(oldState, semantics.contextSignature)
            if (!ret) return false
        }
        return elementFinish(e)
    }

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

            is LatexMathContent -> {
                currentIntro = LatexContextIntro.ASSIGN_MATH
            }
        }
        val oldState = state
        if (currentIntro != null) enterContextIntro(currentIntro)
        var ret = true
        run {
            e.forEachDirectChild {
                if (!traverseRecur(it)) {
                    ret = false
                    return@run // stop processing children
                }
            }
        }
        if (currentIntro != null) exitContextIntro(oldState, currentIntro)
        return ret && elementFinish(e)
    }
}

/**
 * A traverser that keeps track of the current LaTeX context.
 *
 * It can be used in context-aware inspections.
 */
abstract class LatexWithContextTraverser(
    initialState: LContextSet, lookup: LatexSemanticsLookup
) : LatexWithContextStateTraverser<LContextSet>(initialState, lookup) {

    override fun enterContextIntro(intro: LatexContextIntro) {
        state = intro.applyTo(state)
    }

    override fun exitContextIntro(old: LContextSet, intro: LatexContextIntro) {
        state = old
    }
}