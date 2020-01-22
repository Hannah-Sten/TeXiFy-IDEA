package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiReference
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.reference.CommandDefinitionReference
import nl.hannahsten.texifyidea.util.projectSearchScope

/**
 * Get a list of references to definitions of this command.
 */
fun LatexCommands.userDefinedCommandReferences(): List<PsiReference> {
    // Get all commands that define this command.
    val definitionCommands = LatexDefinitionIndex.getCommandsByName(name ?: return emptyList(), project, project.projectSearchScope)
    // Return a reference for this command, which implements the resolve to its definition(s).
    if (definitionCommands.isNotEmpty()) return listOf(CommandDefinitionReference(this))
    return emptyList()
}