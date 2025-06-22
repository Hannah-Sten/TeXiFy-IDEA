package nl.hannahsten.texifyidea.index

import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.TexifyCoroutine
import nl.hannahsten.texifyidea.util.files.documentClassFileInProject
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.findRootFiles
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.isTestProject

/**
 * @author Hannah Schellekens
 */
abstract class IndexUtilBase<T : PsiElement>(

    /**
     * The class of the elements that are stored in the index.
     */
    private val elementClass: Class<T>,

    /**
     * The key of the index.
     */
    private val indexKey: StubIndexKey<String, T>
) {
    // TODO performance improvement
    /** Cache the index items to avoid unnecessary get actions from the index, which take a long time (50-100ms) even for a small index, which can be problematic if index is accessed many times per second. */
    var cache: MutableMap<Project, MutableMap<GlobalSearchScope, Collection<SmartPsiElementPointer<T>>>> = mutableMapOf()

    // Somehow, we need to avoid spamming cache fills because this makes constructing the file set cache very slow
    var lastCacheFillTime = 0L

    private fun buildSearchFiles(baseFile: PsiFile, useIndexCache: Boolean): GlobalSearchScope {
        val searchFiles = baseFile.referencedFileSet(useIndexCache)
            .map { it.virtualFile }
            .toMutableSet()
        searchFiles.add(baseFile.virtualFile)

        // Add document classes
        // There can be multiple, e.g. in the case of subfiles, in which case we probably want all items in the super-fileset
        val roots = baseFile.findRootFiles()
        roots.mapNotNull { it.documentClassFileInProject() }.forEach { documentClass ->
            searchFiles.add(documentClass.virtualFile)
            documentClass.referencedFileSet().asSequence()
                .forEach { searchFiles.add(it.virtualFile) }
        }

        // Search index.
        return GlobalSearchScope.filesScope(baseFile.project, searchFiles.filterNotNull())
    }

    /**
     * Get all the items in the index in the given file set.
     * Consider using [nl.hannahsten.texifyidea.util.files.commandsInFileSet] where applicable.
     *
     * @param baseFile
     *          The file from which to look.
     */
    fun getItemsInFileSet(baseFile: PsiFile, useIndexCache: Boolean = true): Collection<T> {
        // Setup search set.
        val project = baseFile.project
        val scope = buildSearchFiles(baseFile, useIndexCache)
        return getItems(project, scope, useIndexCache)
    }

    fun getFirstItemByNameInFileSet(baseFile: PsiFile, name: String, useIndexCache: Boolean = true): T? {
        // TODO: it is only a temporary solution
        val project = baseFile.project
        val scope = buildSearchFiles(baseFile, useIndexCache)
        return getItemsByName(name, project, scope).firstOrNull { it.isValid }
    }

    /**
     * Get all the items in the index in the given file set, as well as the files where those items are.
     * Consider using [nl.hannahsten.texifyidea.util.files.commandsAndFilesInFileSet] where applicable.
     *
     * @param baseFile The file from which to look.
     * @return List of pairs consisting of a file and the items in that file.
     */
    fun getItemsAndFilesInFileSet(baseFile: PsiFile): List<Pair<PsiFile, Collection<T>>> {
        val result = mutableListOf<Pair<PsiFile, Collection<T>>>()

        // Find all files to search in
        val searchFiles = baseFile.referencedFileSet().toMutableSet()
        val documentclass = baseFile.findRootFile().documentClassFileInProject()
        if (documentclass != null) {
            searchFiles.add(documentclass)
        }

        for (file in searchFiles) {
            val scope = GlobalSearchScope.fileScope(file)
            result.add(Pair(file, getItems(baseFile.project, scope)))
        }

        return result
    }

    /**
     * Get all the items in the index that are in the given file.
     *
     * NOTE: this does not preserve the order of the commands.
     */
    fun getItems(file: PsiFile, useCache: Boolean = true): Collection<T> {
        return if (!file.project.isDisposed) {
            getItems(file.project, GlobalSearchScope.fileScope(file), useCache)
        }
        else {
            emptySet()
        }
    }

    /**
     * Get all the items in the index that are in the given project.
     */
    fun getItems(project: Project, useCache: Boolean = true) = getItems(project, GlobalSearchScope.projectScope(project), useCache)

    /**
     * Get all the items in the index.
     *
     * If [useCache] is false, then the cache will be refreshed.
     *
     * @param project
     *          The project instance.
     * @param scope
     *          The scope in which to search for the items.
     */
    @Deprecated("Use getItemsNonBlocking")
    fun getItems(project: Project, scope: GlobalSearchScope, useCache: Boolean = true): Collection<T> {
        // Cached values may have become invalid over time, so do a double check to be sure (#2976)
        val cachedValues = cache[project]?.get(scope)?.mapNotNull { pointer -> pointer.element }?.filter(PsiElement::isValid)
        if (useCache && cachedValues != null && !project.isTestProject()) {
            // Always trigger a cache refresh anyway, so that at least it won't be outdated for very long
            // If not refreshed often enough, inspections may work on index caches that are still empty (after project start)
            if (System.currentTimeMillis() - lastCacheFillTime > 100) {
                lastCacheFillTime = System.currentTimeMillis()
                TexifyCoroutine.runInBackground {
                    // Same code as below but using smartReadAction(project) instead of runReadAction
                    readAction {
                        val result = getKeys(project).flatMap { getItemsByName(it, project, scope).filter(PsiElement::isValid) }
                        cache.getOrPut(project) { mutableMapOf() }[scope] = result.mapNotNull { if (!it.isValid) null else it.createSmartPointer() }
                    }
                }
            }

            return cachedValues
        }

//        val result = runReadAction { getKeys(project) }.flatMap { runReadAction { getItemsByName(it, project, scope).filter(PsiElement::isValid) } }
        val result = getKeys(project).flatMap { getItemsByName(it, project, scope).filter(PsiElement::isValid) }
        cache.getOrPut(project) { mutableMapOf() }[scope] = result.mapNotNull { if (!it.isValid) null else it.createSmartPointer() }

        // Because the stub index may not always be reliable (#4006), include cached values
        val cached = cachedValues ?: emptyList()
        return (result + cached).toSet()
    }

    /**
     * Get the number of indexed items without using any cache at all.
     */
    fun getNumberOfIndexedItems(project: Project): Int {
        val scope = GlobalSearchScope.projectScope(project)
//        return runReadAction { getKeys(project) }.flatMap { runReadAction { getItemsByName(it, project, scope).filter(PsiElement::isValid) } }.size
        return getKeys(project).flatMap { getItemsByName(it, project, scope).filter(PsiElement::isValid) }.size
    }

    // Same as getItems but runReadAction is replaced by smartReadAction(project)
    suspend fun getItemsNonBlocking(project: Project, scope: GlobalSearchScope, useCache: Boolean = true): Collection<T> {
        // Cached values may have become invalid over time, so do a double check to be sure (#2976)
        val cachedValues = cache[project]?.get(scope)?.let { smartReadAction(project) { it.mapNotNull { pointer -> pointer.element }.filter(PsiElement::isValid) } }
        if (useCache && cachedValues != null) {
            return cachedValues
        }
        val result = smartReadAction(project) { getKeys(project) }.flatMap { smartReadAction(project) { getItemsByName(it, project, scope).filter(PsiElement::isValid) } }
        cache.getOrPut(project) { mutableMapOf() }[scope] = result.mapNotNull { smartReadAction(project) { if (!it.isValid) null else it.createSmartPointer() } }
        // Because the stub index may not always be reliable (#4006), include cached values
        val cached = cachedValues ?: emptyList()
        return (result + cached).toSet()
    }

    /**
     * Get all the items in the index that have the given name.
     * WARNING This takes significant time because of index access. Be very careful about performance when calling it many times.
     *
     * @param name
     *          The name of the items to get.
     * @param project
     *          The project instance.
     * @param scope
     *          The scope in which to search for the items.
     */
    private fun getItemsByName(name: String, project: Project, scope: GlobalSearchScope): Collection<T> {
        try {
            StubIndex.getInstance().processElements(indexKey, name, project, scope, elementClass) {
                true
            }
            return StubIndex.getElements(indexKey, name, project, scope, elementClass)
        }
        catch (e: Exception) {
            // For some reason, any issue from any plugin that causes an exception will be raised here and will be attributed to TeXiFy, flooding the backlog
            // Hence, we just ignore all of them and hope it's not important
            if (e is ProcessCanceledException) {
                throw e
            }
            Log.warn(e.toString())
        }
        catch (e: Throwable) {
            if ("TextMateFileType" in e.toString()) {
                // If users had a LaTeX TextMate bundle before installing TeXiFy, the index may be confused. We can ignore this because the next valid indexing action will fix this, see #4035
                Log.warn(e.toString())
            }
            else {
                throw e
            }
        }
        return emptySet()
    }

    /**
     * Get all the keys in the index.
     *
     * @param project
     *          The project instance.
     */
    private fun getKeys(project: Project): Array<String> {
        if (!DumbService.isDumb(project) && !project.isDefault) {
            try {
                return StubIndex.getInstance().getAllKeys(indexKey, project).toTypedArray()
            }
            catch (e: Exception) {
                // See above
                if (e is ProcessCanceledException) {
                    throw e
                }
                Log.warn(e.toString())
            }
        }
        return emptyArray()
    }

    /**
     * Get the key of the index.
     */
    fun key() = indexKey
}
