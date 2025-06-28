package nl.hannahsten.texifyidea.util.files

import arrow.atomic.AtomicBoolean
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.project.Project
import com.intellij.platform.util.progress.ProgressReporter
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import com.jetbrains.rd.util.concurrentMapOf
import nl.hannahsten.texifyidea.file.listeners.VfsChangeListener
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.util.files.ReferencedFileSetCache.Cache.fileSetCache
import nl.hannahsten.texifyidea.util.runInBackgroundNonBlocking

/**
 * Caches the values for [findReferencedFileSetWithoutCache] calls.
 *
 * @author Hannah Schellekens
 */
class ReferencedFileSetCache {

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
         *
         * We need to keep track per project, because a cache fill can only be done for a given project, so we want to avoid dropping caches for other projects.
         */
        internal var fileSetCache = concurrentMapOf<Project, MutableMap<String, Set<SmartPsiElementPointer<PsiFile>>>>()

        /**
         * Similar to [fileSetCache], maps file paths to root files of the file set.
         */
        internal var rootFilesCache = concurrentMapOf<Project, MutableMap<String, Set<SmartPsiElementPointer<PsiFile>>>>()

        internal var isCacheFillInProgress = concurrentMapOf<Project, AtomicBoolean>()

        /**
         * The number of includes in the include index at the time the cache was last filled.
         * This is used to check if any includes were added or deleted since the last cache fill, and thus if the cache
         * needs to be refreshed.
         *
         * Note that this class is global, so multiple projects can be open.
         */
        internal var numberOfIncludes = concurrentMapOf<Project, Int>()
    }

    /**
     * Get the file set of base file `file`.
     * When the cache is outdated, this will first update the cache.
     */
    @Synchronized
    fun fileSetFor(file: PsiFile, useIndexCache: Boolean = true): Set<PsiFile> {
        return getSetFromCache(file, fileSetCache.getOrPut(file.project) { mutableMapOf() }, useIndexCache)
    }

    @Synchronized
    fun rootFilesFor(file: PsiFile, useIndexCache: Boolean = true): Set<PsiFile> {
        return getSetFromCache(file, Cache.rootFilesCache.getOrPut(file.project) { mutableMapOf() }, useIndexCache)
    }

    /**
     * Note: this will not trigger a cache refill
     */
    fun dropAllCaches(project: Project) {
        fileSetCache[project]?.clear()
        Cache.rootFilesCache[project]?.clear()
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
    private suspend fun getNewCachesFor(project: Project, reporter: ProgressReporter): Pair<Map<String, Set<SmartPsiElementPointer<PsiFile>>>, Map<String, Set<SmartPsiElementPointer<PsiFile>>>> {
        val newFileSetCache = mutableMapOf<String, Set<SmartPsiElementPointer<PsiFile>>>()
        val newRootFilesCache = mutableMapOf<String, Set<SmartPsiElementPointer<PsiFile>>>()

        val filesets = project.findReferencedFileSetWithoutCache(reporter).toMutableMap()
        val tectonicInclusions = findTectonicTomlInclusions(project)

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
                smartReadAction(project) {
                    if (it.isValid) it.createSmartPointer() else null
                }
            }.toSet()
            for (file in fileset) {
                newFileSetCache[file.virtualFile.path] = validFiles
            }

            // Find root files, either this or other files
            val rootFiles = mutableSetOf<PsiFile>()
            for (file in fileset) {
                rootFiles.addAll(file.findRootFilesWithoutCache())
                if (smartReadAction(project) { file.isRoot() }) {
                    rootFiles.add(file)
                }
            }

            val validRootFiles = rootFiles.mapNotNull {
                smartReadAction(project) {
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
    private fun getSetFromCache(file: PsiFile, cache: MutableMap<String, Set<SmartPsiElementPointer<PsiFile>>>, useIndexCache: Boolean = true): Set<PsiFile> {
        if (file.virtualFile == null) {
            return setOf(file)
        }
        val project = file.project

        // Use the keys of the whole project, because suppose a new include includes the current file, it could be anywhere in the project
        // Note that LatexIncludesIndex.Util.getItems(file.project) may be a slow operation and should not be run on EDT
        // Don't use cache here, otherwise we would just be comparing cache with cache
        val numberOfIncludes = NewSpecialCommandsIndex.getAllFileInputs(project).size

        // The cache should be complete once filled, any files not in there are assumed to not be part of a file set that has a valid root file
        if (numberOfIncludes != Cache.numberOfIncludes[project] && !Cache.isCacheFillInProgress.getOrPut(project) { AtomicBoolean(false) }.getAndSet(true)) {
            Cache.numberOfIncludes[project] = numberOfIncludes

            runInBackgroundNonBlocking(project, "Updating file set cache...") { reporter ->
                try {
                    // Only drop caches after we have new data (since that task may be cancelled)
                    // TODO
//
//                    val (newFileSetCache, newRootFilesCache) = getNewCachesFor(project, reporter)
//                    dropAllCaches(project)
//                    fileSetCache.getOrPut(project) { mutableMapOf() }.putAll(newFileSetCache.toMutableMap())
//                    Cache.rootFilesCache.getOrPut(project) { mutableMapOf() }.putAll(newRootFilesCache.toMutableMap())
//                    // Many inspections use the file set, so a rerun could give different results
//                    smartReadAction(project) { file.rerunInspections() }
                }
                finally {
                    Cache.isCacheFillInProgress[project]?.set(false)
                }
            }
        }

        // Make sure to check if file is still valid after retrieving from cache (it may have been deleted)
        val fileset = cache[file.virtualFile.path]?.mapNotNull { it.element }?.filter { it.isValid }?.toSet()
        return if (fileset.isNullOrEmpty()) setOf(file) else fileset
    }
}