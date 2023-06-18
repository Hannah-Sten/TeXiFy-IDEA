package nl.hannahsten.texifyidea.editor

import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEndCommand
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

/**
 * Disable WordSelectioner (on by default) for LatexCommands, because we handle those in [LatexCommandSelectioner].
 */
class CommandSelectionFilter : Condition<PsiElement> {

    override fun value(t: PsiElement?) = !(
        TexifySettings.getInstance().includeBackslashInSelection && (
            t?.firstParentOfType(LatexCommands::class)?.text == t?.text ||
                t?.firstParentOfType(LatexBeginCommand::class)?.textOffset == t?.textOffset ||
                t?.firstParentOfType(LatexEndCommand::class)?.textOffset == t?.textOffset
            )
        )
}