package nl.hannahsten.texifyidea.completion

import arrow.atomic.AtomicBoolean
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.forEachWithProgress
import com.intellij.platform.util.progress.withProgressText
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import kotlinx.coroutines.CoroutineScope
import nl.hannahsten.texifyidea.completion.LatexCommandsAndEnvironmentsCompletionProvider.Companion.createCommandLookupElements
import nl.hannahsten.texifyidea.index.file.LatexExternalCommandIndex
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.util.TexifyCoroutine

/**
 * Cache for [LatexExternalCommandIndex], as index access is very expensive.
 * This cache will not be updated while the IDE is running.
 */
object LatexExternalCommandsIndexCache {
    private val scope: CoroutineScope
        get() = TexifyCoroutine.getInstance().coroutineScope

    private val isCacheFillInProgress = AtomicBoolean(false)

    @Volatile
    var completionElements = setOf<LookupElementBuilder>()

    /**
     * Initiate a cache fill but do not wait for it to be filled.
     */
    fun fillCacheAsync(project: Project, packagesInProject: Set<LatexPackage>) {
        if (DumbService.isDumb(project) || isCacheFillInProgress.compareAndSet(expected = true, new = true)) {
            return
        }
        isCacheFillInProgress.getAndSet(true)

        TexifyCoroutine.runInBackground {
            withBackgroundProgress(project, "Retrieving LaTeX commands...") {
                try {
                    val commandsFromIndex = getIndexedCommandsNoCache(project)
                    completionElements = createLookupElements(commandsFromIndex, packagesInProject)
                }
                finally {
                    isCacheFillInProgress.getAndSet(false)
                }
            }
        }
    }

    /**
     * This may be a very expensive operation, up to one minute for texlive-full
     */
    private suspend fun getIndexedCommandsNoCache(project: Project): MutableList<Set<LatexCommand>> {
        // TODO: Traverse once
        val commands = withProgressText("Getting commands from index...") {
            smartReadAction(project) {
                val commands = mutableListOf<String>()
                FileBasedIndex.getInstance().processAllKeys(
                    LatexExternalCommandIndex.Cache.id,
                    { cmdWithSlash -> commands.add(cmdWithSlash) },
                    GlobalSearchScope.everythingScope(project),
                    null
                )
                commands
            }
        }

        return withProgressText("Processing indexed commands...") {
            val commandsFromIndex = mutableListOf<Set<LatexCommand>>()
            commands.forEachWithProgress { cmdWithSlash ->
                smartReadAction(project) {
                    commandsFromIndex.add(LatexCommand.lookupInIndex(cmdWithSlash, project))
                }
            }
            commandsFromIndex
        }
    }

    private suspend fun createLookupElements(
        commandsFromIndex: MutableList<Set<LatexCommand>>,
        packagesInProject: Set<LatexPackage>,
    ): MutableSet<LookupElementBuilder> {
        // Process each set of command aliases (commands with the same name, but possibly with different arguments) separately.
        return withProgressText("Adding commands to autocompletion...") {
            val lookupElementBuilders = mutableSetOf<LookupElementBuilder>()
            commandsFromIndex.forEachWithProgress { commandAliases ->
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
            lookupElementBuilders
        }
    }
}