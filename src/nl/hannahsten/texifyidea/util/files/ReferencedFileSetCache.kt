package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker

/**
 * Caches the values for [findReferencedFileSet] calls.
 *
 * @author Hannah Schellekens
 */
class ReferencedFileSetCache(val project: Project) {

    private val valueManager = CachedValuesManager.getManager(project)

    /**
     * Tracks the modification counts for each base psi file.
     */
    private val modificationTracker = HashMap<PsiFile, MyModificationTracker>()

    /**
     * The dependencies for the file set cache of the key file.
     *
     * @see CachedValueProvider.Result.getDependencyItems
     */
    private val dependencies = HashMap<PsiFile, Array<Any>>()

    /**
     * A cached file set value for each base file.
     *
     * The base file is the file from which the file set request came from.
     * Meaning that a base file `A` is mapped to the file set with `A` as search root, `B` is mapped to the file set
     * with `B` as search root etc.
     * It could be that multiple values are equal.
     */
    private val fileSetCache = HashMap<PsiFile, CachedValue<Set<PsiFile>>>()

    /**
     * Creates a new cached value for the file set of base file `psiFile`.
     * This will register the cache to the [CachedValuesManager] and store the cached value.
     */
    fun buildCacheFor(psiFile: PsiFile): CachedValue<Set<PsiFile>> {
        val psiFileCache = valueManager.createCachedValue {
            CachedValueProvider.Result.create(findReferencedFileSet(psiFile), dependenciesFor(psiFile))
        }
        fileSetCache[psiFile] = psiFileCache
        return psiFileCache
    }

    /**
     * Get the file set of base file `file`.
     * When the cache is outdated, this will first update the cache.
     */
    fun fileSetFor(file: PsiFile): Set<PsiFile> {
        val cache = fileSetCache[file] ?: buildCacheFor(file)
        return cache.value ?: emptySet()
    }

    /**
     * Clears the cache for base file `file`.
     */
    fun dropCaches(file: PsiFile) {
        modificationTrackerFor(file).myModificationCount++
    }

    /**
     * Also stores the dependencies if they didn't exist yet.
     *
     * @see dependencies
     */
    private fun dependenciesFor(psiFile: PsiFile): Array<Any> {
        return dependencies.getOrPut(psiFile) {
            arrayOf(PsiModificationTracker.MODIFICATION_COUNT, modificationTrackerFor(psiFile))
        }
    }

    /**
     * Also stores the created modification tracker if it didn't exist yet.
     *
     * @see modificationTracker
     */
    private fun modificationTrackerFor(psiFile: PsiFile): MyModificationTracker {
        return modificationTracker.getOrPut(psiFile) { MyModificationTracker() }
    }

    /**
     * Modification tracked used to invalidate the caches.
     *
     * @author Hannah Schellekens
     */
    private class MyModificationTracker : ModificationTracker {

        var myModificationCount: Long = 0

        override fun getModificationCount(): Long = myModificationCount
    }
}