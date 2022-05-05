@file:Suppress("UnusedImport")

package nl.hannahsten.texifyidea.index

import com.intellij.openapi.diagnostic.RuntimeExceptionWithAttachments
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.util.files.documentClassFileInProject
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.referencedFileSet

/**
 * @author Hannah Schellekens
 */
abstract class IndexUtilBase<T : PsiElement>(

    /**
     * The class of the elements that are stored in the index.
     */
    private val elementClass: Class<T>,

    /**
     * The key of the index.
     */
    private val indexKey: StubIndexKey<String, T>
) {

    /** Cache the index items to avoid unnecessary get actions from the index, which take a long time (50-100ms) even for a small index, which can be problematic if index is accessed many times per second. */
    var cache: MutableMap<Project, MutableMap<GlobalSearchScope, Collection<T>>> = mutableMapOf()

    /**
     * Get all the items in the index in the given file set.
     * Consider using [PsiFile.commandsInFileSet] where applicable.
     *
     * @param baseFile
     *          The file from which to look.
     */
    fun getItemsInFileSet(baseFile: PsiFile): Collection<T> {
        // Setup search set.
        val project = baseFile.project
        val searchFiles = baseFile.referencedFileSet().asSequence()
            .map { it.virtualFile }
            .toMutableSet()
        searchFiles.add(baseFile.virtualFile)

        // Add document class.
        val root = baseFile.findRootFile()
        val documentClass = root.documentClassFileInProject()
        if (documentClass != null) {
            searchFiles.add(documentClass.virtualFile)
            documentClass.referencedFileSet().asSequence()
                .forEach { searchFiles.add(it.virtualFile) }
        }

        // Search index.
        val scope = GlobalSearchScope.filesScope(project, searchFiles.filterNotNull())
        return getItems(project, scope)
    }

    /**
     * Get all the items in the index in the given file set, as well as the files where those items are.
     * Consider using [PsiFile.commandsAndFilesInFileSet] where applicable.
     *
     * @param baseFile The file from which to look.
     * @return List of pairs consisting of a file and the items in that file.
     */
    fun getItemsAndFilesInFileSet(baseFile: PsiFile): List<Pair<PsiFile, Collection<T>>> {
        val result = mutableListOf<Pair<PsiFile, Collection<T>>>()

        // Find all files to search in
        val searchFiles = baseFile.referencedFileSet().toMutableSet()
        val documentclass = baseFile.findRootFile().documentClassFileInProject()
        if (documentclass != null) {
            searchFiles.add(documentclass)
        }

        for (file in searchFiles) {
            val scope = GlobalSearchScope.fileScope(file)
            result.add(Pair(file, getItems(baseFile.project, scope)))
        }

        return result
    }

    /**
     * Get all the items in the index that are in the given file.
     *
     * NOTE: this does not preserve the order of the commands.
     */
    fun getItems(file: PsiFile): Collection<T> {
        return if (!file.project.isDisposed) {
            getItems(file.project, GlobalSearchScope.fileScope(file))
        }
        else {
            emptySet()
        }
    }

    /**
     * Get all the items in the index that are in the given project.
     */
    fun getItems(project: Project) = getItems(project, GlobalSearchScope.projectScope(project))

    /**
     * Get all the items in the index.
     *
     * @param project
     *          The project instance.
     * @param scope
     *          The scope in which to search for the items.
     */
    fun getItems(project: Project, scope: GlobalSearchScope): Collection<T> {
        cache[project]?.get(scope)?.let { return it }
        val result = getKeys(project).flatMap { getItemsByName(it, project, scope) }
        cache.getOrPut(project) { mutableMapOf() }[scope] = result
        return result
    }

    /**
     * Get all the items in the index that have the given name.
     *
     * @param name
     *          The name of the items to get.
     * @param project
     *          The project instance.
     * @param scope
     *          The scope in which to search for the items.
     */
    open fun getItemsByName(name: String, project: Project, scope: GlobalSearchScope): Collection<T> {
        try {
            return StubIndex.getElements(indexKey, name, project, scope, elementClass)
        }
        catch (e: RuntimeExceptionWithAttachments) {
            // Ignore, because we've seen it only four times so far (#1375, #1446, #1591, #2086) but I fail to see how this would be a bug in TeXiFy.
            if (e.message?.contains("PSI and index do not match") == false) throw e
        }
        return emptySet()
    }

    /**
     * Get all the keys in the index.
     *
     * @param project
     *          The project instance.
     */
    fun getKeys(project: Project): Array<String> {
        return if (!DumbService.isDumb(project)) {
            StubIndex.getInstance().getAllKeys(indexKey, project).toTypedArray()
        }
        else {
            emptyArray()
        }
    }

    /**
     * Get the key of the index.
     */
    fun key() = indexKey
}
