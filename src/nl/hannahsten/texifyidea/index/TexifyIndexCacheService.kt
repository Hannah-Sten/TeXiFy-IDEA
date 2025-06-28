package nl.hannahsten.texifyidea.index

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

/**
 *
 */
@Service(Service.Level.PROJECT)
class TexifyIndexCacheService(
    val project: Project,
    val myScope: CoroutineScope
) {

    init {
//
//        myScope.launch {
//            updateChannel.consumeEach {
//                withBackgroundProgress(project, "Updating Texify Commands Index") {
//
//                    reportProgress {
//
//                    }
//                }
//            }
//        }
    }

//    /**
//     * Get all the items in the index in the given file set.
//     * Consider using [nl.hannahsten.texifyidea.util.files.commandsInFileSet] where applicable.
//     *
//     * @param baseFile
//     *          The file from which to look.
//     */
//    fun getItemsInFileSet(baseFile: PsiFile): Collection<T> {
//        // Setup search set.
//        val project = baseFile.project
//        val scope = buildSearchFiles(baseFile, useIndexCache)
//        return getItems(project, scope, useIndexCache)
//    }

    companion object {

        fun getInstance(project: Project): TexifyIndexCacheService {
            return project.getService(TexifyIndexCacheService::class.java)
        }
    }
}