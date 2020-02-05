package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiReference
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.reference.CommandDefinitionReference
import nl.hannahsten.texifyidea.util.projectSearchScope

/**
 * Get a list of references to definitions of this command.
 * Will return the empty list if no definitions of this command could be found. // todo wrong?
 */
fun LatexCommands.userDefinedCommandReferences(): List<PsiReference> {
    // Get all commands that define this command.
    val definitionCommands = LatexDefinitionIndex.getCommandsByName(name ?: return emptyList(), project, project.projectSearchScope)
    // Return a reference for this command, which implements the resolve to its definition(s).
    if (definitionCommands.isNotEmpty()) return listOf(CommandDefinitionReference(this))
    return emptyList()

//    LatexDefinitionIndex.getCommandsByNames(Magic.Command.commandDefinitions, project, project.projectSearchScope)
//            .filter { it.requiredParameters.firstOrNull() == name }
}