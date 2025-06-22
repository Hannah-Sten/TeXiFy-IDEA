package nl.hannahsten.texifyidea.index

import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.DelegatingGlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope

class LatexFileFilterScope(delegate : GlobalSearchScope) : DelegatingGlobalSearchScope(delegate) {
    // Following `JavaSourceFilterScope`


    val projectFileIndex : ProjectFileIndex? = project?.let { ProjectRootManager.getInstance(it).fileIndex }


    override fun contains(file: VirtualFile): Boolean {
        if (!super.contains(file)) {
            return false
        }
        if( projectFileIndex == null ) {
            return false
        }
        // TODO implement the module scope
        return true
    }
}