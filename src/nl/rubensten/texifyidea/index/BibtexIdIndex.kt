package nl.rubensten.texifyidea.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.ArrayUtil
import nl.rubensten.texifyidea.psi.BibtexId
import nl.rubensten.texifyidea.util.referencedFiles

/**
 * @author Ruben Schellekens
 */
object BibtexIdIndex : StringStubIndexExtension<BibtexId>() {

    @JvmStatic
    val KEY = StubIndexKey.createIndexKey<String, BibtexId>("nl.rubensten.texifyidea.bibtex.id")

    /**
     * Get all the indexed [BibtexId]s in the project.
     */
    @JvmStatic
    fun getIndexedIds(project: Project) = getIndexedIds(project, GlobalSearchScope.projectScope(project))

    /**
     * Get all the indexed [BibtexId]s in the given file.
     */
    @JvmStatic
    fun getIndexedIds(file: PsiFile) = getIndexedIds(file.project, GlobalSearchScope.fileScope(file))

    /**
     * Get all the indexed [BibtexId]s in the complete file set of the given base file.
     *
     * @param baseFile
     *          The file whose file set must be analysed for indexed ids.
     */
    @JvmStatic
    fun getIndexedIdsInFileSet(baseFile: PsiFile): Collection<BibtexId> {
        val project = baseFile.project
        val searchFiles: MutableSet<VirtualFile> = baseFile.referencedFiles()
                .map(PsiFile::getVirtualFile)
                .toMutableSet()
        searchFiles.add(baseFile.virtualFile)
        val scope = GlobalSearchScope.filesScope(project, searchFiles)
        return getIndexedIds(project, scope)
    }

    /**
     * Get all the indexed [BibtexId]s in the given search scope.
     */
    @JvmStatic
    fun getIndexedIds(project: Project, scope: GlobalSearchScope): Collection<BibtexId> {
        val commands: MutableCollection<BibtexId> = ArrayList()

        for (key in getKeys(project)) {
            commands.addAll(getIdByName(key, project, scope));
        }

        return commands
    }

    @JvmStatic
    fun getIdByName(name: String, project: Project, scope: GlobalSearchScope): Collection<BibtexId> {
        return StubIndex.getElements(KEY, name, project, scope, BibtexId::class.java)
    }

    @JvmStatic
    fun getKeys(project: Project): Array<String> {
        val index = StubIndex.getInstance()
        val keys = index.getAllKeys(KEY, project)
        return ArrayUtil.toStringArray(keys)
    }

    override fun getKey() = KEY
}