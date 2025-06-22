package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.concurrency.annotations.RequiresReadLock
import nl.hannahsten.texifyidea.grammar.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.documentClassFileInProject
import nl.hannahsten.texifyidea.util.files.findRootFiles
import nl.hannahsten.texifyidea.util.files.referencedFileSet

/**
 * A stub index that also collects all the related elements.
 *
 * Generally, this is not a good idea to iterate over all the related elements, but it seems to be the only way for LaTex commands and environments.
 */
abstract class CollectAllStubIndexBase<Psi : PsiElement>(
    val clazz: Class<Psi>,
) : StringStubIndexExtension<Psi>() {

    abstract val keyForAll: String

    protected open fun wrapSearchScope(scope: GlobalSearchScope): GlobalSearchScope {
        return scope
    }

    protected open fun buildSearchFiles(
        baseFile: PsiFile,
    ): GlobalSearchScope {
        return GlobalSearchScope.fileScope(baseFile)
    }

    fun sinkIndex(sink: IndexSink, indexKey: StubIndexKey<String, Psi>, commandToken: String) {
        sink.occurrence(indexKey, commandToken)
        sink.occurrence(indexKey, keyForAll)
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

    @RequiresReadLock
    fun getAllKeys(project: Project, scope: GlobalSearchScope = GlobalSearchScope.projectScope(project)): Set<String> {
        val res: Collection<String> = StubIndex.getInstance().getAllKeys(key, project)
        return res as? Set<String> ?: res.toSet()
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
    fun traverseByName(
        name: String,
        project: Project,
        scope: GlobalSearchScope = GlobalSearchScope.projectScope(project),
        action: (Psi) -> Boolean
    ): Boolean {
        return StubIndexKt.traverseElements(key, name, project, wrapSearchScope(scope), action)
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
}

abstract class NewLatexCommandsStubIndex : CollectAllStubIndexBase<LatexCommands>(LatexCommands::class.java) {

    final override val keyForAll: String
        get() = KEY_ALL_COMMANDS

    companion object {
        /**
         * A special key to retrieve all commands in the index.
         */
        const val KEY_ALL_COMMANDS: String = "_ALL_"
    }

    override fun getVersion(): Int {
        return LatexParserDefinition.Cache.FILE.stubVersion
    }

    override fun wrapSearchScope(scope: GlobalSearchScope): GlobalSearchScope {
        return LatexFileFilterScope(scope)
    }

    override fun buildSearchFiles(baseFile: PsiFile): GlobalSearchScope {
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

    fun getAllInFileSet(file: PsiFile): Collection<LatexCommands> {
        // Setup search set.
        val project = file.project
        val scope = buildSearchFiles(file)
        return getAll(project, scope)
    }

    fun getInFileSet(file: PsiFile, commandToken: String): Collection<LatexCommands> {
        // Setup search set.
        val project = file.project
        val scope = buildSearchFiles(file)
        return StubIndex.getElements(key, commandToken, project, wrapSearchScope(scope), clazz)
    }
}