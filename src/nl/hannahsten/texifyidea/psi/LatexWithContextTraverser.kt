package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexContextIntro
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.parser.forEachDirectChild

abstract class LatexWithContextTraverser<S>(
    initialState: S,
    protected val lookup: LatexSemanticsLookup
) {

    protected enum class WalkAction {
        CONTINUE,
        SKIP_CHILDREN,
        STOP_WALK
    }

    protected var state: S = initialState

    protected open fun elementStart(e: PsiElement): WalkAction {
        return WalkAction.CONTINUE
    }

    protected abstract fun enterContextIntro(intro: LatexContextIntro)

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
        return true
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
        return true
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
        return ret
    }
}

abstract class Latex