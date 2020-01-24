package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiReference
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