package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.util.ArrayUtil
import nl.hannahsten.texifyidea.psi.BibtexId
import nl.hannahsten.texifyidea.util.files.referencedFileSet

/**
 * @author Hannah Schellekens
 */
object BibtexIdIndex : StringStubIndexExtension<BibtexId>() {

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
        val searchFiles: MutableSet<VirtualFile> = baseFile.referencedFileSet()
                .asSequence()
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
            commands.addAll(getIdByName(key, project, scope))
        }

        return commands
    }

    @JvmStatic
    fun getIdByName(name: String, project: Project, scope: GlobalSearchScope): Collection<BibtexId> {
        return StubIndex.getElements(key, name, project, scope, BibtexId::class.java)
    }

    @JvmStatic
    fun getKeys(project: Project): Array<String> {
        if (DumbService.isDumb(project)) {
            return emptyArray()
        }

        val index = StubIndex.getInstance()
        val keys = index.getAllKeys(key, project)
        return ArrayUtil.toStringArray(keys)
    }

    override fun getKey() = BibtexIdIndexKey.KEY!!
}