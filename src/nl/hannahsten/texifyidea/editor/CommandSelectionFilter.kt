package nl.hannahsten.texifyidea.editor

import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.firstParentOfType

/**
 * Disable WordSelectioner (on by default) for LatexCommands, because we handle those in [LatexCommandSelectioner].
 */
class CommandSelectionFilter : Condition<PsiElement> {
    override fun value(t: PsiElement?) = t?.firstParentOfType(LatexCommands::class)?.text != t?.text
}