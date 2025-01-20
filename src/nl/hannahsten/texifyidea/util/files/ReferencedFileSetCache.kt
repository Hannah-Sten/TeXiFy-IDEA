package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.hannahsten.texifyidea.file.listeners.VfsChangeListener
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import java.util.concurrent.ConcurrentHashMap

/**
 * Caches the values for [findReferencedFileSetWithoutCache] calls.
 *
 * @author Hannah Schellekens
 */
class ReferencedFileSetCache {

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
    private val fileSetCache = ConcurrentHashMap<String, Set<SmartPsiElementPointer<PsiFile>>>()

    private val rootFilesCache = ConcurrentHashMap<String, Set<SmartPsiElementPointer<PsiFile>>>()

    /**
     * The number of includes in the include index at the time the cache was last filled.
     * This is used to check if any includes were added or deleted since the last cache fill, and thus if the cache
     * needs to be refreshed.
     *
     * Note that this class is global, so multiple projects can be open.
     */
    private var numberOfIncludes = mutableMapOf<Project, Int>()

    companion object {

        private val mutex = Mutex()
    }

    /**
     * Get the file set of base file `file`.
     * When the cache is outdated, this will first update the cache.
     */
    @Synchronized
    fun fileSetFor(file: PsiFile): Set<PsiFile> {
        return getSetFromCache(file, fileSetCache)
    }

    @Synchronized
    fun rootFilesFor(file: PsiFile): Set<PsiFile> {
        return getSetFromCache(file, rootFilesCache)
    }

    /**
     * Clears the cache for base file `file`.
     */
    fun dropCaches(file: VirtualFile) {
        fileSetCache.remove(file.path)
        rootFilesCache.remove(file.path)
    }

    fun dropAllCaches() {
        fileSetCache.keys.forEach { fileSetCache.remove(it) }
        rootFilesCache.keys.forEach { rootFilesCache.remove(it) }
    }

    /**
     * Since we have to calculate the fileset to fill the root file or fileset cache, we make sure to only do that
     * once and then fill both caches with all the information we have.
     */
    private fun updateCachesFor(requestedFile: PsiFile) {
        val filesets = requestedFile.project.findReferencedFileSetWithoutCache().toMutableMap()
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
            for (file in fileset) {
                fileSetCache[file.virtualFile.path] = fileset.map { it.createSmartPointer() }.toSet()
            }

            val rootfiles = requestedFile.findRootFilesWithoutCache(fileset)
            for (file in fileset) {
                rootFilesCache[file.virtualFile.path] = rootfiles.map { it.createSmartPointer() }.toSet()
            }
        }
    }

    /**
     * In a thread-safe way, get the value from the cache and if needed refresh the cache first.
     */
    private fun getSetFromCache(file: PsiFile, cache: ConcurrentHashMap<String, Set<SmartPsiElementPointer<PsiFile>>>): Set<PsiFile> {
        return if (file.virtualFile != null) {
            // getOrPut cannot be used because it will still execute the defaultValue function even if the key is already in the map (see its javadoc)
            // Wrapping the code with synchronized (myLock) { ... } also didn't work
            // Hence we use a mutex to make sure the expensive findReferencedFileSet function is only executed when needed
            runBlocking {
                // Do NOT use a coroutine here, because then when typing (so the caller is e.g. gutter icons, inspections, line makers etc.) somehow the following (at least the runReadAction parts) will block the UI. Note that certain user-triggered actions (think run configuration) will still lead to this blocking the UI if not run in the background explicitly
                mutex.withLock {
                    // Use the keys of the whole project, because suppose a new include includes the current file, it could be anywhere in the project
                    // Note that LatexIncludesIndex.Util.getItems(file.project) may be a slow operation and should not be run on EDT
                    val includes = LatexIncludesIndex.Util.getItems(file.project)

                    // The cache should be complete once filled, any files not in there are assumed to not be part of a file set that has a valid root file
                    if (includes.size != numberOfIncludes[file.project]) {
                        numberOfIncludes[file.project] = includes.size
                        dropAllCaches()
                        updateCachesFor(file)
                    }
                }
            }
            // Make sure to check if file is still valid after retrieving from cache (it may have been deleted)
            cache[file.virtualFile.path]?.mapNotNull { it.element }?.filter { it.isValid }?.toSet() ?: setOf(file)
        }
        else {
            setOf(file)
        }
    }
}