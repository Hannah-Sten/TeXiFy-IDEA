package nl.hannahsten.texifyidea.util.parser

import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.psi.*

/*
 * This file contains utility functions paralleling [Psi] but with improved performance.
 *
 * ## Read Access
 *
 * The methods in this file are assumed read access to the PSI tree.
 *
 * Typically, PSI related extensions are called from a read action, so we must avoid wrapping these methods in a read action.
 */

/**
 * Determines whether any parent of this PsiElement matches the given predicate.
 */
inline fun PsiElement.anyParent(predicate: (PsiElement) -> Boolean): Boolean {
    var parent: PsiElement? = this.parent
    while (parent != null) {
        if (predicate(parent)) {
            return true
        }
        parent = parent.parent
    }
    return false
}

/**
 * Checks if the psi element is in math mode or not.
 *
 * **This function is slow as it checks all parents of the psi element.**
 *
 * @return `true` when the element is in math mode, `false` when the element is in no math mode.
 */
fun PsiElement.inMathContext(): Boolean {
    // TODO: performance
    // 1st improved version, test if any parent element is in a math environment
    return anyParent { it is LatexMathEnvMarker }
        || anyParent { it.inDirectEnvironmentContext(Environment.Context.MATH) } // can be improved
}

/**
 * Traverse the PSI tree and apply the action to each command element.
 *
 * @param action The action to apply to each [LatexCommands] element found in the PSI tree.
 */
fun PsiElement.traverseCommands(action: (LatexCommands) -> Unit)  {
    // Traverse the PSI tree and apply the action to each command element
    this.accept(LatexCommandTraverser(action))
}

/**
 * Iterate through all direct children of the PsiElement and apply the action to each child
 *
 * @param action The action to apply to each child element. If the action returns false, the iteration stops.
 */
fun PsiElement.forEachDirectChild(action: (PsiElement) -> Boolean) : Boolean {
    for(child in this.children) {
        if(!action(child)) return false
    }
    return true
}

/**
 * Apply the given action to each [LatexRequiredParam] in the command.
 *
 * @param action The action to apply to each [LatexRequiredParam] element found in the command. If the action returns false, the traversal stops.
 */
fun LatexCommandWithParams.traverseRequiredParams(action: (PsiElement) -> Boolean) {
    /*
    Recall the bnf:
    commands ::= COMMAND_TOKEN parameter*
    parameter ::= optional_param | required_param | picture_param | ANGLE_PARAM
     */
    forEachDirectChild { param ->
        param.forEachDirectChild {
            if (it is LatexRequiredParam) {
                val res = action(it)
                if(!res) return@forEachDirectChild false
            }
            true
        }
    }
}