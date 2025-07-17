package nl.hannahsten.texifyidea.index

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.*
import com.intellij.util.Processor
import com.intellij.util.Processors
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.indexing.IdFilter
import nl.hannahsten.texifyidea.util.contentSearchScope

/**
 *
 */
abstract class MyStringStubIndexBase<Psi : PsiElement>(
    val clazz: Class<Psi>,
) : StringStubIndexExtension<Psi>() {

    protected open fun buildFileset(
        baseFile: PsiFile,
    ): GlobalSearchScope {
        return GlobalSearchScope.fileScope(baseFile)
    }

    @RequiresReadLock
    fun getByName(
        name: String, project: Project, scope: GlobalSearchScope = project.contentSearchScope
    ): Collection<Psi> {
        return StubIndex.getElements(key, name, project, scope, clazz)
    }

    @RequiresReadLock
    fun getByName(
        name: String, project: Project, file: VirtualFile
    ): Collection<Psi> {
        return getByName(name, project, GlobalSearchScope.fileScope(project, file))
    }

    @RequiresReadLock
    fun countByName(
        name: String,
        project: Project,
        scope: GlobalSearchScope = project.contentSearchScope
    ): Int {
        var count = 0
        forEachByName(name, project, scope) {
            count++
        }
        return count
    }

    @RequiresReadLock
    fun existsByName(
        name: String,
        project: Project,
        scope: GlobalSearchScope = project.contentSearchScope
    ): Boolean {
        val tr = traverseByName(name, project, scope) {
            false // Stop traversing when the first element is found
        }
        return !tr // If the traversal returned false, it means no elements were found
    }

    @RequiresReadLock
    fun getByName(
        name: String,
        scope: GlobalSearchScope
    ): Collection<Psi> {
        return getByName(name, scope.project!!, scope)
    }

    @RequiresReadLock
    fun getByName(name: String, file: PsiFile): Collection<Psi> {
        val project = file.project
        if (project.isDisposed) return emptyList()
        return getByName(name, project, GlobalSearchScope.fileScope(file))
    }

    @RequiresReadLock
    fun getByNames(
        names: Collection<String>, project: Project,
        scope: GlobalSearchScope = project.contentSearchScope
    ): List<Psi> {
        return names.flatMap { name ->
            StubIndex.getElements(key, name, project, scope, clazz)
        }
    }

    @RequiresReadLock
    fun getByNames(
        names: Collection<String>, project: Project, file: VirtualFile,
    ): List<Psi> {
        return getByNames(names, project, GlobalSearchScope.fileScope(project, file))
    }

    @RequiresReadLock
    fun getByNames(
        names: Collection<String>, file: PsiFile,
    ): List<Psi> {
        val project = file.project
        if (project.isDisposed) return emptyList()
        return getByNames(names, project, GlobalSearchScope.fileScope(file))
    }

    @RequiresReadLock
    fun getAllKeys(scope: GlobalSearchScope): Set<String> {
        val res = hashSetOf<String>()
        processAllKeys(scope, processor = Processors.cancelableCollectProcessor(res))
        return res
    }

    @RequiresReadLock
    fun processAllKeys(scope: GlobalSearchScope, idFilter: IdFilter? = null, processor: Processor<in String>): Boolean {
        return StubIndex.getInstance().processAllKeys(key, processor, scope, idFilter)
    }

    @RequiresReadLock
    override fun getAllKeys(project: Project): Set<String> {
        return getAllKeys(project.contentSearchScope)
    }

    @RequiresReadLock
    override fun processAllKeys(project: Project, processor: Processor<in String>): Boolean {
        return processAllKeys(project.contentSearchScope, processor = processor)
    }

    @RequiresReadLock
    fun forEachKey(
        project: Project,
        scope: GlobalSearchScope = project.contentSearchScope,
        action: (String) -> Unit
    ) {
        processAllKeys(scope, idFilter = null) {
            ProgressManager.checkCanceled()
            action(it)
            true // Continue processing
        }
    }

    @RequiresReadLock
    fun processByName(name: String, project: Project, scope: GlobalSearchScope = project.contentSearchScope, idFilter: IdFilter?, processor: Processor<in Psi>): Boolean {
        return StubIndex.getInstance().processElements(key, name, project, scope, idFilter, clazz, processor)
    }

    /**
     * Traverses all elements with the given name in the index, and applies the action to each of them,
     * until the action returns false or all elements have been traversed.
     *
     * @return true if all elements were traversed, false if the action returned false for any element.
     */
    @RequiresReadLock
    fun traverseByName(
        name: String,
        project: Project,
        scope: GlobalSearchScope = project.contentSearchScope,
        action: (Psi) -> Boolean
    ): Boolean {
        return StubIndexKt.traverseElements(key, name, project, scope, action)
    }

    @RequiresReadLock
    fun forEachByName(
        name: String,
        project: Project,
        scope: GlobalSearchScope = project.contentSearchScope,
        filter: IdFilter? = null,
        action: (Psi) -> Unit
    ) {
        StubIndexKt.forEachElement(key, name, project, scope, action)
    }

    fun getByNameInFileSet(name: String, file: PsiFile): Collection<Psi> {
        // Setup search set.
        val project = file.project
        val scope = buildFileset(file)
        return StubIndex.getElements(key, name, project, scope, clazz)
    }

    fun getByNamesInFileSet(name: Set<String>, file: PsiFile): Collection<Psi> {
        val project = file.project
        val scope = buildFileset(file)
        return getByNames(name, project, scope)
    }
}

interface MyTransformedStubIndex<Stub : StubElement<Psi>, Psi : PsiElement> : StubIndexExtension<String, Psi> {

    fun sinkIndex(stub: Stub, sink: IndexSink)
}