package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.listeners.VfsChangeListener

/**
 * Caches the values for [findReferencedFileSet] calls.
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
    private val fileSetCache = HashMap<VirtualFile, Set<PsiFile>>()

    /**
     * Get the file set of base file `file`.
     * When the cache is outdated, this will first update the cache.
     */
    @Synchronized
    fun fileSetFor(file: PsiFile): Set<PsiFile> {
        return if (file.virtualFile != null) {
            fileSetCache.getOrPut(file.virtualFile) { findReferencedFileSet(file) }
        }
        else {
            emptySet()
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