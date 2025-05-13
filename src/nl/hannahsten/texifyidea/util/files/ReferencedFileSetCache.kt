package nl.hannahsten.texifyidea.util.files

import arrow.atomic.AtomicBoolean
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.util.progress.ProgressReporter
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import kotlinx.coroutines.sync.Mutex
import nl.hannahsten.texifyidea.file.listeners.VfsChangeListener
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.util.runInBackgroundNonBlocking
import java.util.concurrent.ConcurrentHashMap

/**
 * Caches the values for [findReferencedFileSetWithoutCache] calls.
 *
 * @author Hannah Schellekens
 */
class ReferencedFileSetCache {

    companion object {

        private val mutex = Mutex()
    }

    object Cache {

        /**
         * A cached file set value for each base file.
         *
         * The base file is the file from which the file set request came from.
         * Meaning that a base file `A` is mapped to the file set with `A` as search root, `B` is mapped to the file set
         * with `B` as search root etc.
         * It could be that multiple values are equal.
         *
         * We use the file path as key instead of VirtualFile, because there may be different VirtualFiles pointing to the same file on disk. See [VfsChangeListener].
         * For the same reason we do not use a CachedValue, because the CachedValuesManager is project-specific.
         *
         * We use SmartPsiElementPointer to avoid storing files which have become invalid, e.g. after installing a plugin which doesn't require a restart.
         */
        internal var fileSetCache = ConcurrentHashMap<String, Set<SmartPsiElementPointer<PsiFile>>>()

        internal var rootFilesCache = ConcurrentHashMap<String, Set<SmartPsiElementPointer<PsiFile>>>()

        internal var isCacheFillInProgress = AtomicBoolean(false)

        /**
         * The number of includes in the include index at the time the cache was last filled.
         * This is used to check if any includes were added or deleted since the last cache fill, and thus if the cache
         * needs to be refreshed.
         *
         * Note that this class is global, so multiple projects can be open.
         */
        internal var numberOfIncludes = mutableMapOf<Project, Int>()
    }

    /**
     * Get the file set of base file `file`.
     * When the cache is outdated, this will first update the cache.
     */
    @Synchronized
    fun fileSetFor(file: PsiFile, useIndexCache: Boolean = true): Set<PsiFile> {
        return getSetFromCache(file, Cache.fileSetCache, useIndexCache)
    }

    @Synchronized
    fun rootFilesFor(file: PsiFile, useIndexCache: Boolean = true): Set<PsiFile> {
        return getSetFromCache(file, Cache.rootFilesCache, useIndexCache)
    }

    /**
     * Clears the cache for base file `file`.
     * Note: this will not trigger a cache refill
     */
    fun dropCaches(file: VirtualFile) {
        Cache.fileSetCache.remove(file.path)
        Cache.rootFilesCache.remove(file.path)
    }

    /**
     * Note: this will not trigger a cache refill
     */
    fun dropAllCaches() {
        Cache.fileSetCache.clear()
        Cache.rootFilesCache.clear()
    }

    /**
     * Cache will not be dropped immediately, but a cache refresh will be started the next time data is requested.
     */
    fun markCacheOutOfDate() {
        Cache.numberOfIncludes.clear()
    }

    /**
     * Since we have to calculate the fileset to fill the root file or fileset cache, we make sure to only do that
     * once and then fill both caches with all the information we have.
     */
    private suspend fun getNewCachesFor(requestedFile: PsiFile, reporter: ProgressReporter?): Pair<Map<String, Set<SmartPsiElementPointer<PsiFile>>>, Map<String, Set<SmartPsiElementPointer<PsiFile>>>> {
        val newFileSetCache = mutableMapOf<String, Set<SmartPsiElementPointer<PsiFile>>>()
        val newRootFilesCache = mutableMapOf<String, Set<SmartPsiElementPointer<PsiFile>>>()

        val filesets = requestedFile.project.findReferencedFileSetWithoutCache(reporter).toMutableMap()
        val tectonicInclusions = findTectonicTomlInclusions(requestedFile.project)

        // Now we join all the file sets that are in the same file set according to the Tectonic.toml file
        for (inclusionsSet in tectonicInclusions) {
            val mappings = filesets.filter { it.value.intersect(inclusionsSet).isNotEmpty() }
            val newFileSet = mappings.values.flatten().toSet() + inclusionsSet
            mappings.forEach {
                filesets[it.key] = newFileSet
            }
        }

        for (fileset in filesets.values) {
            val validFiles = fileset.mapNotNull {
                smartReadAction(requestedFile.project) {
                    if (it.isValid) it.createSmartPointer() else null
                }
            }.toSet()
            for (file in fileset) {
                newFileSetCache[file.virtualFile.path] = validFiles
            }

            val rootFiles = requestedFile.findRootFilesWithoutCache(fileset)
            val validRootFiles = rootFiles.mapNotNull {
                smartReadAction(requestedFile.project) {
                    if (it.isValid) it.createSmartPointer() else null
                }
            }.toSet()
            for (file in fileset) {
                newRootFilesCache[file.virtualFile.path] = validRootFiles
            }
        }

        return Pair(newFileSetCache, newRootFilesCache)
    }

    /**
     * In a thread-safe way, get the value from the cache and if needed refresh the cache first.
     */
    private fun getSetFromCache(file: PsiFile, cache: ConcurrentHashMap<String, Set<SmartPsiElementPointer<PsiFile>>>, useIndexCache: Boolean = true): Set<PsiFile> {
        if (file.virtualFile == null) {
            return setOf(file)
        }

        // Use the keys of the whole project, because suppose a new include includes the current file, it could be anywhere in the project
        // Note that LatexIncludesIndex.Util.getItems(file.project) may be a slow operation and should not be run on EDT
        val includes = LatexIncludesIndex.Util.getItems(file.project, useCache = useIndexCache)

        // The cache should be complete once filled, any files not in there are assumed to not be part of a file set that has a valid root file
        if (includes.size != Cache.numberOfIncludes[file.project] && !Cache.isCacheFillInProgress.getAndSet(true)) {
            Cache.numberOfIncludes[file.project] = includes.size

            runInBackgroundNonBlocking(file.project, "Updating file set cache...") { reporter ->
                try {
                    // Only drop caches after we have new data (since that task may be cancelled)
                    val (newFileSetCache, newRootFilesCache) = getNewCachesFor(file, reporter)
                    dropAllCaches()
                    Cache.fileSetCache.putAll(ConcurrentHashMap(newFileSetCache.toMutableMap()))
                    Cache.rootFilesCache.putAll(ConcurrentHashMap(newRootFilesCache.toMutableMap()))
                    // Many inspections use the file set, so a rerun could give different results
                    smartReadAction(file.project) { file.rerunInspections() }
                }
                finally {
                    Cache.isCacheFillInProgress.set(false)
                }
            }
        }

        // Make sure to check if file is still valid after retrieving from cache (it may have been deleted)
        val fileset = cache[file.virtualFile.path]?.mapNotNull { it.element }?.filter { it.isValid }?.toSet()
        return if (fileset.isNullOrEmpty())setOf(file) else fileset
    }
}