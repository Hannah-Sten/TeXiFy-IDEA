package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.ID
import com.intellij.util.indexing.IdFilter
import nl.hannahsten.texifyidea.util.everythingScope

open class FileBasedIndexRetriever<K : Any, V : Any>(
    val id: ID<K, V>
) {

    fun processAllKeys(
        scope: GlobalSearchScope,
        idFilter: IdFilter? = null,
        processor: Processor<K>
    ): Boolean {
        return FileBasedIndex.getInstance().processAllKeys(id, processor, scope, idFilter)
    }

    inline fun forEachKey(scope: GlobalSearchScope, crossinline action: (K) -> Unit) {
        processAllKeys(scope, null) {
            action(it)
            true
        }
    }

    fun getAllKeys(project: Project): Set<K> {
        val res: Collection<K> = FileBasedIndex.getInstance().getAllKeys(id, project)
        return (res as? Set<K>) ?: res.toSet()
    }

    fun getAllKeys(
        scope: GlobalSearchScope,
    ): Set<K> {
        val result = hashSetOf<K>()
        forEachKey(scope) { result.add(it) }
        return result
    }

    fun getValuesByKey(key: K, scope: GlobalSearchScope): List<V> {
        return FileBasedIndex.getInstance().getValues(id, key, scope)
    }

    fun getValuesByKey(key: K, project: Project): List<V> {
        return FileBasedIndex.getInstance().getValues(id, key, GlobalSearchScope.everythingScope(project))
    }

    fun getByKey(key: K, scope: GlobalSearchScope): List<Pair<V, VirtualFile>> {
        val result = arrayListOf<Pair<V, VirtualFile>>()
        forEachByKey(key, scope) { virtualFile, value ->
            result.add(value to virtualFile)
        }
        return result
    }

    fun getContainingFiles(key: K, scope: GlobalSearchScope): Collection<VirtualFile> {
        return FileBasedIndex.getInstance().getContainingFiles(id, key, scope)
    }

    fun getContainingFiles(key: K, project: Project): Collection<VirtualFile> {
        return getContainingFiles(key, project.everythingScope)
    }

    fun processByKey(key: K, scope: GlobalSearchScope, action: (VirtualFile, V) -> Boolean): Boolean {
        return FileBasedIndex.getInstance().processValues(id, key, null, action, scope)
    }

    inline fun forEachByKey(
        key: K,
        scope: GlobalSearchScope,
        crossinline action: (VirtualFile, V) -> Unit
    ) {
        processByKey(key, scope) { virtualFile, value ->
            action(virtualFile, value)
            true
        }
    }

    fun processFilesByKeys(keys: Set<K>, scope: GlobalSearchScope, action: (VirtualFile) -> Boolean): Boolean {
        return FileBasedIndex.getInstance().processFilesContainingAnyKey(id, keys, scope, null, null, action)
    }
}
