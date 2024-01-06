package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import nl.hannahsten.texifyidea.completion.LatexCommandsAndEnvironmentsCompletionProvider.Companion.createCommandLookupElements
import nl.hannahsten.texifyidea.index.file.LatexExternalCommandIndex
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexCommand

/**
 * Cache for [LatexExternalCommandIndex], as index access is very expensive.
 * This cache will not be updated while the IDE is running.
 */
object LatexExternalCommandsIndexCache {
    private val scope = CoroutineScope(Dispatchers.Default)

    var externalCommands = listOf<Set<LatexCommand>>()
    var completionElements = setOf<LookupElementBuilder>()

    fun unload() {
        scope.cancel()
    }

    /**
     * Initiate a cache fill but do not wait for it to be filled.
     * todo mutex
     */
    fun fillCacheAsync(project: Project, packagesInProject: List<LatexPackage>) {
        scope.launch {
            ProgressManager.getInstance().run(object : Backgroundable(project, "Getting commands from index...") {
                override fun run(indicator: ProgressIndicator) {
                    getIndexedCommandsNoCache(project, packagesInProject, indicator)
                }
            })

        }
    }

    /**
     * This may be a very expensive operation, up to one minute for texlive-full
     */
    private fun getIndexedCommandsNoCache(project: Project, packagesInProject: List<LatexPackage>, indicator: ProgressIndicator) {

        indicator.text = "Getting commands from index..."
        val commands = mutableListOf<String>()
        runReadAction {
            FileBasedIndex.getInstance().processAllKeys(
                LatexExternalCommandIndex.Cache.id,
                { cmdWithSlash -> commands.add(cmdWithSlash) },
                GlobalSearchScope.everythingScope(project),
                null
            )
        }

        indicator.text = "Processing indexed commands..."
        var progress = 0
        val commandsFromIndex = mutableListOf<Set<LatexCommand>>()
        for ((index, cmdWithSlash) in commands.withIndex()) {
            indicator.fraction = index.toDouble() / commands.size
            commandsFromIndex.add(LatexCommand.lookupInIndex(cmdWithSlash.substring(1), project))
        }

        externalCommands = commandsFromIndex // todo refactor

        createLookupElements(commandsFromIndex, packagesInProject, indicator)
    }

    private fun createLookupElements(
        commandsFromIndex: MutableList<Set<LatexCommand>>,
        packagesInProject: List<LatexPackage>,
        indicator: ProgressIndicator
    ) {
        indicator.text = "Creating lookup elements..."
        val lookupElementBuilders = mutableSetOf<LookupElementBuilder>()

        // Process each set of command aliases (commands with the same name, but possibly with different arguments) separately.
        commandsFromIndex.mapIndexed { index, commandAliases ->
            indicator.fraction = index.toDouble() / commandsFromIndex.size
            commandAliases.filter { command -> if (LatexCommandsAndEnvironmentsCompletionProvider.isTexliveAvailable) command.dependency in packagesInProject else true }
                .forEach { cmd ->
                    createCommandLookupElements(cmd)
                        // Avoid duplicates of commands defined in LaTeX base, because they are often very similar commands defined in different document classes, so it makes not
                        // much sense at the moment to have them separately in the autocompletion.
                        // Effectively this results in just taking the first one we found
                        .filter { newBuilder ->
                            if (cmd.dependency.isDefault) {
                                lookupElementBuilders.none { it.lookupString == newBuilder.lookupString }
                            }
                            else {
                                true
                            }
                        }.forEach { lookupElementBuilders.add(it) }
                }
        }

        completionElements = lookupElementBuilders
    }


}