package nl.hannahsten.texifyidea.util.labels

import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.lang.alias.CommandManager
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.runInBackgroundWithoutProgress

/**
 * All commands that represent a reference to a label, including user defined commands.
 */
fun Project.getLabelReferenceCommands(): Set<String> {
    CommandManager.updateAliases(CommandMagic.labelReferenceWithoutCustomCommands.keys, this)
    return CommandManager.getAliases(CommandMagic.labelReferenceWithoutCustomCommands.keys.first())
}

/**
 * Get all commands defining labels, including user defined commands. This will not check if the aliases need to be updated.
 */
fun getLabelDefinitionCommandsNoUpdate() = CommandManager.getAliases(CommandMagic.labels.first())

/**
 * Get all commands defining labels, including user defined commands.
 * If you need to know which parameters of user defined commands define a label, use [CommandManager.labelAliasesInfo].
 *
 * This will check if the cache of user defined commands needs to be updated, based on the given project, and therefore may take some time.
 */
fun Project.getLabelDefinitionCommands(): Set<String> {
    // Check if updates are needed
    CommandManager.updateAliases(CommandMagic.labels, this)
    return CommandManager.getAliases(CommandMagic.labels.first())
}

/**
 * See [getLabelDefinitionCommands], but will not wait until the update is finished.
 */
fun Project.getLabelDefinitionCommandsAndUpdateLater(): Set<String> {
    // Check if updates are needed
    runInBackgroundWithoutProgress {
        smartReadAction(this) {
            CommandManager.updateAliases(CommandMagic.labels, this)
        }
    }
    return CommandManager.getAliases(CommandMagic.labels.first())
}
