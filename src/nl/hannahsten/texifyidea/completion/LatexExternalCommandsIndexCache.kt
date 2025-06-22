package nl.hannahsten.texifyidea.completion

import arrow.atomic.AtomicBoolean
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.DumbService
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

    private val isCacheFillInProgress = AtomicBoolean(false)

    var completionElements = setOf<LookupElementBuilder>()

    fun unload() {
        scope.cancel()
    }

    /**
     * Initiate a cache fill but do not wait for it to be filled.
     */
    fun fillCacheAsync(project: Project, packagesInProject: Set<LatexPackage>) {
        if (DumbService.isDumb(project) || isCacheFillInProgress.compareAndSet(expected = true, new = true)) {
            return
        }
        isCacheFillInProgress.getAndSet(true)

        scope.launch {
            ProgressManager.getInstance().run(object : Backgroundable(project, "Retrieving LaTeX commands...") {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        val commandsFromIndex = getIndexedCommandsNoCache(project, indicator)
                        completionElements = createLookupElements(commandsFromIndex, packagesInProject, indicator)
                    }
                    finally {
                        isCacheFillInProgress.getAndSet(false)
                    }
                }
            })
        }
    }

    /**
     * This may be a very expensive operation, up to one minute for texlive-full
     */
    private fun getIndexedCommandsNoCache(project: Project, indicator: ProgressIndicator): MutableList<Set<LatexCommand>> {
        indicator.text = "Getting commands from index..."
        val commands = mutableListOf<String>()
        DumbService.getInstance(project).tryRunReadActionInSmartMode({
            FileBasedIndex.getInstance().processAllKeys(
                LatexExternalCommandIndex.Cache.id,
                { cmdWithSlash -> commands.add(cmdWithSlash) },
                GlobalSearchScope.everythingScope(project),
                null
            )
        }, indicator.text)

        indicator.text = "Processing indexed commands..."
        val commandsFromIndex = mutableListOf<Set<LatexCommand>>()
        for ((index, cmdWithSlash) in commands.withIndex()) {
            indicator.checkCanceled()
            indicator.fraction = index.toDouble() / commands.size
            commandsFromIndex.add(LatexCommand.lookupInIndex(cmdWithSlash.substring(1), project))
        }

        return commandsFromIndex
    }

    private fun createLookupElements(
        commandsFromIndex: MutableList<Set<LatexCommand>>,
        packagesInProject: Set<LatexPackage>,
        indicator: ProgressIndicator
    ): MutableSet<LookupElementBuilder> {
        indicator.text = "Adding commands to autocompletion..."
        val lookupElementBuilders = mutableSetOf<LookupElementBuilder>()

        // Process each set of command aliases (commands with the same name, but possibly with different arguments) separately.
        commandsFromIndex.mapIndexed { index, commandAliases ->
            indicator.fraction = index.toDouble() / commandsFromIndex.size
            indicator.checkCanceled()
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

        return lookupElementBuilders
    }
}