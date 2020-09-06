package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
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
     * We use VirtualFile as key instead of PsiFile, because the file set depends on virtual files,
     * but virtual files are not project-specific (can be opened in multiple projects). See [VfsChangeListener].
     * For the same reason we do not use a CachedValue, because the CachedValuesManager is project-specific.
     */
    private val fileSetCache = ConcurrentHashMap<VirtualFile, Set<PsiFile>>()

    /**
     * The number of includes in the include index at the time the cache was last filled.
     * This is used to check if any includes were added or deleted since the last cache fill, and thus if the cache
     * needs to be refreshed.
     */
    private var numberOfIncludes = 0

    private val mutex = Mutex()

    /**
     * Get the file set of base file `file`.
     * When the cache is outdated, this will first update the cache.
     */
    @Synchronized
    fun fileSetFor(file: PsiFile): Set<PsiFile> {
        return if (file.virtualFile != null) {
            // Use the keys of the whole project, because suppose a new include includes the current file, it could be anywhere in the project
            val numberOfIncludesChanged = if (LatexIncludesIndex.getItems(file.project).size != numberOfIncludes) {
                numberOfIncludes = LatexIncludesIndex.getItems(file.project).size
                dropAllCaches()
                true
            }
            else {
                false
            }

            // getOrPut cannot be used because it will still execute the defaultValue function even if the key is already in the map (see its javadoc)
            // Wrapping the code with synchronized (myLock) { ... } also didn't work
            // Hence we use a mutex to make sure the expensive findReferencedFileSet function is only executed when needed
            runBlocking {
                mutex.withLock {
                    if (!fileSetCache.containsKey(file.virtualFile) || numberOfIncludesChanged) {
                        runReadAction {
                            fileSetCache[file.virtualFile] = findReferencedFileSetWithoutCache(file)
                        }
                    }
                }
            }
            fileSetCache[file.virtualFile] ?: setOf(file)
        }
        else {
            setOf(file)
        }
    }

    /**
     * Clears the cache for base file `file`.
     */
    fun dropCaches(file: VirtualFile) {
        fileSetCache.remove(file)
    }

    fun dropAllCaches() {
        fileSetCache.keys.forEach { fileSetCache.remove(it) }
    }
}