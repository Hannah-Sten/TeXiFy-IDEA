package nl.hannahsten.texifyidea.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.lang.alias.CommandManager
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import java.util.concurrent.atomic.AtomicBoolean


// Due to the update method being called many times, we need to limit the number of updates requested
var isUpdatingIncludeAliases = AtomicBoolean(false)

fun updateAndGetIncludeCommands(project: Project): Set<String> {
    // For performance reasons, do not wait until the update (which requires index access) is done
    updateIncludeCommandsAliasesAsync(project)
    return defaultIncludeCommands.map { CommandManager.getAliases(it) }.flatten().toSet()
}

fun updateIncludeCommandsAliasesAsync(project: Project) {
    if (!isUpdatingIncludeAliases.getAndSet(true)) {
        // Don't run with progress indicator, because this takes a short time (a few tenths) and runs in practice on every letter typed
        ApplicationManager.getApplication().invokeLater {
            try {
                // Because every command has different parameters and behaviour (e.g. allowed file types), we keep track of them separately
                for (command in defaultIncludeCommands) {
                    runReadAction {
                        CommandManager.updateAliases(setOf(command), project)
                    }
                }
            }
            finally {
                isUpdatingIncludeAliases.set(false)
            }
        }
    }
}

/**
 * Given a possible alias of an include command, find a random original command it is an alias of
 */
fun getOriginalCommandFromAlias(commandName: String, project: Project): LatexCommand? {
    val aliasSet = CommandManager.getAliases(commandName)
    if (aliasSet.isEmpty()) {
        // If we can't find anything, trigger an update so that maybe we have the information next time
        updateIncludeCommandsAliasesAsync(project)
    }
    return LatexCommand.lookup(aliasSet.firstOrNull { it in defaultIncludeCommands })?.first()
}