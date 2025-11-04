package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiInvalidElementAccessException
import com.intellij.psi.util.elementType
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNoMathContent
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import nl.hannahsten.texifyidea.util.parser.nextSiblingOfType
import nl.hannahsten.texifyidea.util.parser.parentOfType
import nl.hannahsten.texifyidea.util.parser.previousSiblingOfType

/**
 * If the element is in an if/else construct
 *
 * @return `true` if the element is in a conditional branch, `false` otherwise.
 * Returns `false` if PSI tree traversal fails (e.g., due to outdated stub indices or invalid PSI elements).
 */
fun isInConditionalBranch(element: PsiElement): Boolean {
    return try {
        // \ifthenelse{condition}{true}{false}
        val inIfthenelse = element.firstParentOfType<LatexParameter>()?.firstParentOfType<LatexCommands>()?.name == LatexGenericRegularCommand.IFTHENELSE.commandWithSlash
        if (inIfthenelse) {
            true
        }
        else {
            // Check for an \if...\fi combination
            isPreviousConditionalStart(element) && isNextConditionalEnd(element)
        }
    }
    catch (e: IndexNotReadyException) {
        // Index is not ready yet, assume not in conditional branch
        false
    }
    catch (e: PsiInvalidElementAccessException) {
        // PSI element is invalid, assume not in conditional branch
        false
    }
    catch (e: Exception) {
        // Catch other PSI-related exceptions (e.g., "Outdated stub in index")
        // In this case, assume the element is not in a conditional branch.
        // The inspection will be re-run once indices are updated.
        false
    }
}

/**
 * If the next relevant command is a \fi
 */
private fun isNextConditionalEnd(current: PsiElement): Boolean {
    return isEndConditional(nextConditionalCommand(current, searchBackwards = false) ?: return false)
}

/**
 * If the previous relevant command is an \if
 */
private fun isPreviousConditionalStart(current: PsiElement): Boolean {
    return isStartConditional(nextConditionalCommand(current, searchBackwards = true) ?: return false)
}

/**
 * Next relevant command. There are  many ways in which this does not work, but since this is just an inspection this is much safer than trying to parse user defined \if commands in the parser, which is impossiblee
 */
private fun nextConditionalCommand(element: PsiElement, searchBackwards: Boolean): PsiElement? {
    var current = element.parentOfType(LatexNoMathContent::class)
    while (current != null && !isConditional(current)) {
        current = if (!searchBackwards) {
            current.nextSibling?.nextSiblingOfType(LatexNoMathContent::class)
        }
        else {
            current.prevSibling?.previousSiblingOfType(LatexNoMathContent::class)
        }
    }
    return current
}

private fun isConditional(element: PsiElement): Boolean {
    return isStartConditional(element) || isEndConditional(element)
}

private fun isStartConditional(rootElement: PsiElement): Boolean {
    // To keep it simple, only look one level down
    for (element in rootElement.children + listOf(rootElement)) {
        if (element is LatexCommands && element.name?.startsWith("\\if") == true) return true
        if (element.elementType == LatexTypes.START_IF) return true
    }
    return false
}

private fun isEndConditional(rootElement: PsiElement): Boolean {
    for (element in rootElement.children + listOf(rootElement)) {
        if (element.firstChild?.elementType in setOf(LatexTypes.ELSE, LatexTypes.END_IF)) return true
    }
    return false
}