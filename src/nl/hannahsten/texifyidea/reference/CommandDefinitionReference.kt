package nl.hannahsten.texifyidea.reference

import com.intellij.psi.*
import com.intellij.util.containers.toArray
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.forcedFirstRequiredParameterAsCommand
import nl.hannahsten.texifyidea.util.projectSearchScope

/**
 * @author Abby Berkers
 */
class CommandDefinitionReference(element: LatexCommands) : PsiReferenceBase<LatexCommands>(element), PsiPolyVariantReference {
    init {
        rangeInElement = ElementManipulators.getValueTextRange(element)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = element.project
        return LatexDefinitionIndex.getCommandsByNames(Magic.Command.commandDefinitions, project, project.projectSearchScope)
                .filter { it.forcedFirstRequiredParameterAsCommand()?.name == element.commandToken.text }
                .map { PsiElementResolveResult(it.commandToken) }
                .toArray(emptyArray())
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }
}