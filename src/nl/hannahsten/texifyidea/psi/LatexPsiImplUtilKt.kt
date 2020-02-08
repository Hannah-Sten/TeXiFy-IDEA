package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.paths.WebReference
import com.intellij.psi.PsiReference
import com.intellij.util.containers.toArray
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.reference.CommandDefinitionReference
import nl.hannahsten.texifyidea.util.forcedFirstRequiredParameterAsCommand
import nl.hannahsten.texifyidea.util.projectSearchScope

/**
 * Get a list of references to definitions of this command.
 */
fun LatexCommands.userDefinedCommandReferences(): List<PsiReference> {
    val allUserDefinedCommands: Collection<LatexCommands> = LatexDefinitionIndex
            .getCommandsByNames(LatexPsiImplUtil.DEFINITION_COMMANDS, project, project.projectSearchScope)

    if (allUserDefinedCommands.map { it.forcedFirstRequiredParameterAsCommand()?.name }.contains(commandToken.text)) {
        return listOf(CommandDefinitionReference(this))
    }
    return emptyList()
}

fun LatexCommands.extractUrlReferences(firstParam: LatexRequiredParam): Array<PsiReference> =
        LatexPsiImplUtil.extractSubParameterRanges(firstParam)
                .map { WebReference(this, it.shiftRight(firstParam.textOffset - textOffset)) }
                .toArray(emptyArray())