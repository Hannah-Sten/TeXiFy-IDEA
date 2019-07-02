package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
 * @author Hannah Schellekens
 */
interface ReferencedFileSetService {

    companion object {

        @JvmStatic
        fun getInstance(project: Project): ReferencedFileSetService {
            return ServiceManager.getService(project, ReferencedFileSetService::class.java)
        }
    }

    /**
     * [findReferencedFileSet], but then with cached values.
     */
    fun referencedFileSetOf(psiFile: PsiFile): Set<PsiFile>

    /**
     * Invalidates the caches for the given file.
     */
    fun dropCaches(psiFile: PsiFile)
}