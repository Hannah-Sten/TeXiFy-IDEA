package nl.hannahsten.texifyidea.util

import com.intellij.psi.PsiElement
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
 */
fun isInConditionalBranch(element: PsiElement): Boolean {
    // \ifthenelse{condition}{true}{false}
    if (element.firstParentOfType<LatexParameter>()?.firstParentOfType<LatexCommands>()?.name == LatexGenericRegularCommand.IFTHENELSE.commandWithSlash) {
        return true
    }

    // Check for an \if...\fi combination
    return isPreviousConditionalStart(element) && isNextConditionalEnd(element)
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