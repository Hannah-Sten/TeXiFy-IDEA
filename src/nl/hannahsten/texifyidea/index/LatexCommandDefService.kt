package nl.hannahsten.texifyidea.index

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineScope
import nl.hannahsten.texifyidea.util.BasicBackgroundCacheService

/**
 * Provide a unified definition service for LaTeX commands, including
 *   * those hard-coded in the plugin, see [nl.hannahsten.texifyidea.util.magic.CommandMagic], [nl.hannahsten.texifyidea.lang.commands.LatexRegularCommand]
 *   * those indexed by file-based index [nl.hannahsten.texifyidea.index.file.LatexExternalCommandIndex]
 *   * those indexed by stub-based index [nl.hannahsten.texifyidea.index.NewDefinitionIndex]
 */
@Service(Service.Level.PROJECT)
class LatexCommandDefService(
    project: Project,
    coroutineScope: CoroutineScope
) : BasicBackgroundCacheService<Fileset, Any>(coroutineScope) {



    override suspend fun computeValue(key: Fileset): Any? {
        persistentSetOf<Any>().add(key)
        TODO()
    }
}