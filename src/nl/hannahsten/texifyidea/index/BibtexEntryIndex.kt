package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.util.ArrayUtil
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.util.files.referencedFileSet

/**
 * @author Hannah Schellekens
 */
class BibtexEntryIndex : StringStubIndexExtension<BibtexEntry>() {

    /**
     * Get all the indexed [BibtexEntry]s in the project.
     */
    fun getIndexedEntries(project: Project) = getIndexedEntries(project, GlobalSearchScope.projectScope(project))

    /**
     * Get all the indexed [BibtexEntry]s in the given file.
     */
    fun getIndexedEntries(file: PsiFile) = getIndexedEntries(file.project, GlobalSearchScope.fileScope(file))

    /**
     * Get all the indexed [BibtexEntry]s in the complete file set of the given base file.
     *
     * @param baseFile
     *          The file whose file set must be analysed for indexed ids.
     */
    fun getIndexedEntriesInFileSet(baseFile: PsiFile): Collection<BibtexEntry> {
        val project = baseFile.project
        val searchFiles: MutableSet<VirtualFile> = baseFile.referencedFileSet()
            .asSequence()
            .mapNotNull(PsiFile::getVirtualFile)
            .toMutableSet()
        if (baseFile.virtualFile != null) {
            searchFiles.add(baseFile.virtualFile)
        }
        val scope = GlobalSearchScope.filesScope(project, searchFiles)
        return getIndexedEntries(project, scope)
    }

    /**
     * Get all the indexed [BibtexEntry]s in the given search scope.
     */
    private fun getIndexedEntries(project: Project, scope: GlobalSearchScope): Collection<BibtexEntry> {
        val commands: MutableCollection<BibtexEntry> = ArrayList()

        for (key in getKeys(project)) {
            commands.addAll(getEntryByName(key, project, scope))
        }

        return commands
    }

    fun getEntryByName(name: String, project: Project, scope: GlobalSearchScope): Collection<BibtexEntry> {
        return StubIndex.getElements(key, name, project, scope, BibtexEntry::class.java)
    }

    private fun getKeys(project: Project): Array<String> {
        if (DumbService.isDumb(project)) {
            return emptyArray()
        }

        val index = StubIndex.getInstance()
        val keys = index.getAllKeys(key, project)
        return ArrayUtil.toStringArray(keys)
    }

    override fun getKey() = BibtexEntryIndexKey.KEY
}