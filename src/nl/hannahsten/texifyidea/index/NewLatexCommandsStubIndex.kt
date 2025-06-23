package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.Processor
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.indexing.IdFilter
import nl.hannahsten.texifyidea.grammar.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.documentClassFileInProject
import nl.hannahsten.texifyidea.util.files.findRootFiles
import nl.hannahsten.texifyidea.util.files.referencedFileSet

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
        scope: GlobalSearchScope = GlobalSearchScope.projectScope(project)
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
        scope: GlobalSearchScope = GlobalSearchScope.projectScope(project)
    ): List<Psi> {
        return names.flatMap { name ->
            getByName(name, project, scope)
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
    fun getAllKeys(project: Project, scope: GlobalSearchScope = GlobalSearchScope.projectScope(project)): Set<String> {
        val res: Collection<String> = StubIndex.getInstance().getAllKeys(key, project)
        if (res is Set<String>) return res
        return res.toSet()
    }

    @RequiresReadLock
    fun processAllKeys(scope: GlobalSearchScope, processor: Processor<in String>): Boolean {
        return StubIndex.getInstance().processAllKeys(key, processor, scope)
    }

    @RequiresReadLock
    override fun getAllKeys(project: Project): Set<String> {
        return getAllKeys(project, GlobalSearchScope.projectScope(project))
    }

    @RequiresReadLock
    override fun processAllKeys(project: Project, processor: Processor<in String>): Boolean {
        return processAllKeys(GlobalSearchScope.projectScope(project), processor)
    }

    @RequiresReadLock
    fun processByName(name: String, project: Project, scope: GlobalSearchScope = GlobalSearchScope.projectScope(project), idFilter: IdFilter?, processor: Processor<in Psi>): Boolean {
        return StubIndex.getInstance().processElements(key, name, project, wrapSearchScope(scope), idFilter, clazz, processor)
    }

    @RequiresReadLock
    fun traverseByName(
        name: String,
        project: Project,
        scope: GlobalSearchScope = GlobalSearchScope.projectScope(project),
        action: (Psi) -> Boolean
    ): Boolean {
        return StubIndexKt.traverseElements(key, name, project, wrapSearchScope(scope), action)
    }

    @RequiresReadLock
    fun forEachByName(
        name: String,
        project: Project,
        scope: GlobalSearchScope = GlobalSearchScope.projectScope(project),
        filter: IdFilter? = null,
        action: (Psi) -> Unit
    ) {
        StubIndexKt.forEachElement(key, name, project, wrapSearchScope(scope), action)
    }
}

abstract class SpecialKeyStubIndexBase<Psi : PsiElement>(clazz: Class<Psi>) : MyStringStubIndexBase<Psi>(clazz) {
    protected abstract val specialKeys: Set<String>
    protected abstract val keyForAll: String
    protected abstract val specialKeyMap: Map<String, List<String>>

    fun sinkIndex(sink: IndexSink, commandToken: String) {
        val indexKey = key
        sink.occurrence(indexKey, commandToken)
        sink.occurrence(indexKey, keyForAll)
        specialKeyMap[commandToken]?.let { keys ->
            keys.forEach {
                sink.occurrence(indexKey, it)
            }
        }
    }

    @RequiresReadLock
    fun traverseAll(
        project: Project,
        scope: GlobalSearchScope = GlobalSearchScope.projectScope(project),
        action: (Psi) -> Boolean
    ): Boolean {
        return StubIndexKt.traverseElements(key, keyForAll, project, wrapSearchScope(scope), action)
    }

    @RequiresReadLock
    fun traverseAll(file: PsiFile, action: (Psi) -> Boolean): Boolean {
        val project = file.project
        if (project.isDisposed) return true
        return traverseAll(project, GlobalSearchScope.fileScope(file), action)
    }

    /**
     * Applies the action to each element in the index.
     */
    @RequiresReadLock
    fun forEachAll(
        project: Project,
        scope: GlobalSearchScope = GlobalSearchScope.projectScope(project),
        action: (Psi) -> Unit
    ) {
        StubIndexKt.forEachElement(key, keyForAll, project, wrapSearchScope(scope), action)
    }

    @RequiresReadLock
    fun collectAll(
        project: Project,
        scope: GlobalSearchScope = GlobalSearchScope.projectScope(project),
        filter: (Psi) -> Boolean
    ): List<Psi> {
        val results = mutableListOf<Psi>()
        traverseAll(project, scope) { element ->
            if (filter(element)) {
                results.add(element)
            }
            true
        }
        return results
    }

    @RequiresReadLock
    fun getAll(project: Project, scope: GlobalSearchScope = GlobalSearchScope.projectScope(project)): Collection<Psi> {
        return StubIndex.getElements(
            key, keyForAll, project, wrapSearchScope(scope), clazz
        )
    }

    @RequiresReadLock
    fun getAll(file: PsiFile): Collection<Psi> {
        val project = file.project
        if (project.isDisposed) return emptyList()
        return getAll(project, GlobalSearchScope.fileScope(file))
    }

    fun getAllInFileSet(file: PsiFile): Collection<Psi> {
        // Setup search set.
        val project = file.project
        val scope = buildSearchFiles(file)
        return getAll(project, scope)
    }
}

fun buildLatexSearchFiles(baseFile: PsiFile): GlobalSearchScope {
    // TODO improve it
    val useIndexCache = true
    val searchFiles = baseFile.referencedFileSet(useIndexCache)
        .mapNotNullTo(mutableSetOf()) { it.virtualFile }
    searchFiles.add(baseFile.virtualFile)

    // Add document classes
    // There can be multiple, e.g., in the case of subfiles, in which case we probably want all items in the super-fileset
    val roots = baseFile.findRootFiles()
    for (root in roots) {
        val docClass = root.documentClassFileInProject() ?: continue
        searchFiles.add(docClass.virtualFile)
        docClass.referencedFileSet(useIndexCache).forEach {
            searchFiles.add(it.virtualFile)
        }
    }

    // Search index.
    return GlobalSearchScope.filesScope(baseFile.project, searchFiles)
}

abstract class NewLatexCommandsStubIndex : MyStringStubIndexBase<LatexCommands>(LatexCommands::class.java) {

    abstract override fun getKey(): StubIndexKey<String, LatexCommands>

    override fun getVersion(): Int {
        return LatexParserDefinition.Cache.FILE.stubVersion
    }

    override fun wrapSearchScope(scope: GlobalSearchScope): GlobalSearchScope {
        return LatexFileFilterScope(scope)
    }

    override fun buildSearchFiles(baseFile: PsiFile): GlobalSearchScope {
        return buildLatexSearchFiles(baseFile)
    }

    fun getInFileSet(file: PsiFile, commandToken: String): Collection<LatexCommands> {
        // Setup search set.
        val project = file.project
        val scope = buildSearchFiles(file)
        return StubIndex.getElements(key, commandToken, project, wrapSearchScope(scope), clazz)
    }
}