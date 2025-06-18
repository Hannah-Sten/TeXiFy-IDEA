package nl.hannahsten.texifyidea.index

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgress
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import nl.hannahsten.texifyidea.util.files.documentClassFileInProject
import nl.hannahsten.texifyidea.util.files.findRootFiles
import nl.hannahsten.texifyidea.util.files.referencedFileSet


@Service(Service.Level.PROJECT)
class TexifyIndexCacheService(
    val project : Project,
    val myScope: CoroutineScope) {

    private val updateChannel = Channel<Unit>(Channel.CONFLATED)


    private fun buildSearchFiles(baseFile: PsiFile, useIndexCache: Boolean): GlobalSearchScope {
        val searchFiles = baseFile.referencedFileSet(useIndexCache)
            .mapNotNullTo(mutableSetOf()) { it.virtualFile }
        searchFiles.add(baseFile.virtualFile)

        // Add document classes
        // There can be multiple, e.g., in the case of subfiles, in which case we probably want all items in the super-fileset
        val roots = baseFile.findRootFiles()
        for( root in roots) {
            val docClass = root.documentClassFileInProject() ?: continue
            searchFiles.add(docClass.virtualFile)
            docClass.referencedFileSet(useIndexCache).forEach {
                searchFiles.add(it.virtualFile)
            }
        }

        // Search index.
        return GlobalSearchScope.filesScope(baseFile.project, searchFiles)
    }

    init{
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
    companion object{

        fun getInstance(project: Project): TexifyIndexCacheService {
            return project.getService(TexifyIndexCacheService::class.java)
        }
    }
}