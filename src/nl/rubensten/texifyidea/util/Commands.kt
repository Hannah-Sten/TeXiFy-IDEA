package nl.rubensten.texifyidea.util

import com.intellij.openapi.project.Project
import nl.rubensten.texifyidea.index.LatexDefinitionIndex
import nl.rubensten.texifyidea.psi.LatexCommands

/**
 * Finds all defined commands within the project.
 *
 * @return The found commands.
 */
fun Project.findCommandDefinitions(): Collection<LatexCommands> {
    return LatexDefinitionIndex.getItems(this).filter {
        it.name in Magic.Command.commandDefinitions
    }
}