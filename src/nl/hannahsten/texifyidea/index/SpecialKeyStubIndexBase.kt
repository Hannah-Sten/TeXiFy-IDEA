package nl.hannahsten.texifyidea.index

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IndexSink

abstract class SpecialKeyStubIndexBase<Psi : PsiElement>(clazz: Class<Psi>) : MyStringStubIndexBase<Psi>(clazz) {
    protected abstract val specialKeys: Set<String>
    protected abstract val specialKeyMap: Map<String, List<String>>

    fun sinkIndex(sink: IndexSink, name: String) {
        val indexKey = key
        sink.occurrence(indexKey, name)
        specialKeyMap[name]?.let { keys ->
            keys.forEach {
                sink.occurrence(indexKey, it)
            }
        }
    }

//    @RequiresReadLock
//    fun traverseAll(
//        project: Project,
//        scope: GlobalSearchScope = GlobalSearchScope.projectScope(project),
//        action: (Psi) -> Boolean
//    ): Boolean {
//        return StubIndexKt.traverseElements(key, keyForAll, project, wrapSearchScope(scope), action)
//    }
//
//    @RequiresReadLock
//    fun traverseAll(file: PsiFile, action: (Psi) -> Boolean): Boolean {
//        val project = file.project
//        if (project.isDisposed) return true
//        return traverseAll(project, GlobalSearchScope.fileScope(file), action)
//    }
//
//    /**
//     * Applies the action to each element in the index.
//     */
//    @RequiresReadLock
//    fun forEachAll(
//        project: Project,
//        scope: GlobalSearchScope = GlobalSearchScope.projectScope(project),
//        action: (Psi) -> Unit
//    ) {
//        StubIndexKt.forEachElement(key, keyForAll, project, wrapSearchScope(scope), action)
//    }
//
//    @RequiresReadLock
//    fun collectAll(
//        project: Project,
//        scope: GlobalSearchScope = GlobalSearchScope.projectScope(project),
//        filter: (Psi) -> Boolean
//    ): List<Psi> {
//        val results = mutableListOf<Psi>()
//        traverseAll(project, scope) { element ->
//            if (filter(element)) {
//                results.add(element)
//            }
//            true
//        }
//        return results
//    }

//    @RequiresReadLock
//    fun getAll(project: Project, scope: GlobalSearchScope = GlobalSearchScope.projectScope(project)): Collection<Psi> {
//        return StubIndex.getElements(
//            key, keyForAll, project, wrapSearchScope(scope), clazz
//        )
//    }
//
//    @RequiresReadLock
//    fun getAll(file: PsiFile): Collection<Psi> {
//        val project = file.project
//        if (project.isDisposed) return emptyList()
//        return getAll(project, GlobalSearchScope.fileScope(file))
//    }
//
//    fun getAllInFileSet(file: PsiFile): Collection<Psi> {
//        // Setup search set.
//        val project = file.project
//        val scope = buildSearchFiles(file)
//        return getAll(project, scope)
//    }
}