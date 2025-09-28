package nl.hannahsten.texifyidea.util.labels

import nl.hannahsten.texifyidea.lang.alias.CommandManager
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * Get all commands defining labels, including user defined commands.
 * If you need to know which parameters of user defined commands define a label, use [CommandManager.labelAliasesInfo].
 *
 * This will check if the cache of user defined commands needs to be updated, based on the given project, and therefore may take some time.
 */
fun getLabelDefinitionCommands(): Set<String> {
    // TODO: replace using DefinitionService
    // Check if updates are needed
    return CommandMagic.labels
}