package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.Project
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

    protected open fun wrapSearchScope(scope: GlobalSearchScope): GlobalSearchScope {
        return scope
    }

    protected open fun buildSearchFiles(
        baseFile: PsiFile,
    ): GlobalSearchScope {
        return GlobalSearchScope.fileScope(baseFile)
    }

    @RequiresReadLock
    fun getByName(
        name: String,
        project: Project,
        scope: GlobalSearchScope = project.contentSearchScope
    ): Collection<Psi> {
        return StubIndex.getElements(key, name, project, wrapSearchScope(scope), clazz)
    }

    @RequiresReadLock
    fun getByName(name: String, file: PsiFile): Collection<Psi> {
        val project = file.project
        if (project.isDisposed) return emptyList()
        return getByName(name, project, GlobalSearchScope.fileScope(file))
    }

    @RequiresReadLock
    fun getByNames(
        names: Collection<String>,
        project: Project,
        scope: GlobalSearchScope = project.contentSearchScope
    ): List<Psi> {
        val wrappedScope = wrapSearchScope(scope)
        return names.flatMap { name ->
            StubIndex.getElements(key, name, project, wrappedScope, clazz)
        }
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
    fun processByName(name: String, project: Project, scope: GlobalSearchScope = project.contentSearchScope, idFilter: IdFilter?, processor: Processor<in Psi>): Boolean {
        return StubIndex.getInstance().processElements(key, name, project, wrapSearchScope(scope), idFilter, clazz, processor)
    }

    @RequiresReadLock
    fun traverseByName(
        name: String,
        project: Project,
        scope: GlobalSearchScope = project.contentSearchScope,
        action: (Psi) -> Boolean
    ): Boolean {
        return StubIndexKt.traverseElements(key, name, project, wrapSearchScope(scope), action)
    }

    @RequiresReadLock
    fun forEachByName(
        name: String,
        project: Project,
        scope: GlobalSearchScope = project.contentSearchScope,
        filter: IdFilter? = null,
        action: (Psi) -> Unit
    ) {
        StubIndexKt.forEachElement(key, name, project, wrapSearchScope(scope), action)
    }

    fun getByNameInFileSet(name: String, file: PsiFile): Collection<Psi> {
        // Setup search set.
        val project = file.project
        val scope = buildSearchFiles(file)
        return StubIndex.getElements(key, name, project, wrapSearchScope(scope), clazz)
    }

    fun getByNamesInFileSet(name: Set<String>, file: PsiFile): Collection<Psi> {
        val project = file.project
        val scope = buildSearchFiles(file)
        return getByNames(name, project, scope)
    }
}

interface MyTransformedStubIndex<Stub : StubElement<Psi>, Psi : PsiElement> : StubIndexExtension<String, Psi> {

    fun sinkIndex(stub: Stub, sink: IndexSink)
}