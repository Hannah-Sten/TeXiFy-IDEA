package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LAssignContext
import nl.hannahsten.texifyidea.lang.LContextInherit
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexContextIntro
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexSemanticLookup
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.parser.forEachDirectChild

abstract class LatexWithContextTraverser<S>(protected val lookup: LatexSemanticLookup) {

    protected enum class WalkAction {
        CONTINUE, SKIP_CHILDREN, STOP_WALK
    }

    protected open fun elementStart(e: PsiElement, state: S): WalkAction {
        return WalkAction.CONTINUE
    }

    protected abstract fun enterContextIntro(s: S, intro: LatexContextIntro): S

    protected abstract fun exitContextIntro(old: S, intro: LatexContextIntro)

    protected fun traverseCommandRecur(e: LatexCommandWithParams, args: List<LArgument>, state: S): Boolean {
        LatexPsiUtil.processArgumentsWithSemantics(e, args) { parameter, argument ->
            val intro = argument?.contextSignature ?: LContextInherit
            val newState = enterContextIntro(state, intro)
            val action = traverseRecur(parameter, newState)
            exitContextIntro(state, intro)
            if (!action) return false
        }
        return true
    }

    protected fun traverseEnvironmentRecur(e: LatexEnvironment, semantics: LSemanticEnv, state: S): Boolean {
        if (!traverseCommandRecur(e.beginCommand, semantics.arguments, state)) return false
        e.environmentContent?.let {
            val contentState = enterContextIntro(state, semantics.contextSignature)
            val ret = traverseRecur(it, contentState)
            exitContextIntro(state, semantics.contextSignature)
            if (!ret) return false
        }
        return true
    }

    protected fun traverseRecur(e: PsiElement, state: S): Boolean {
        val action = elementStart(e, state)
        when (action) {
            WalkAction.STOP_WALK -> return false
            WalkAction.SKIP_CHILDREN -> return true
            else -> { /* CONTINUE */
            }
        }

        var currentIntro: LatexContextIntro? = null

        when (e) {
            is LatexCommands -> {
                val semantic = lookup.lookupCommand(e.name ?: "")
                if (semantic != null) {
                    return traverseCommandRecur(e, semantic.arguments, state)
                }
            }

            is LatexEnvironment -> {
                val semantic = lookup.lookupEnv(e.getEnvironmentName())
                if (semantic != null) {
                    return traverseEnvironmentRecur(e, semantic, state)
                }
            }

            is LatexMathContent -> {
                currentIntro = LAssignContext(LatexContexts.Math)
            }
        }
        val childState = if (currentIntro != null) enterContextIntro(state, currentIntro) else state
        var ret = true
        run {
            e.forEachDirectChild {
                if (!traverseRecur(it, childState)) {
                    ret = false
                    return@run // stop processing children
                }
            }
        }
        if (currentIntro != null) exitContextIntro(state, currentIntro)
        return ret
    }
}
/*


 */