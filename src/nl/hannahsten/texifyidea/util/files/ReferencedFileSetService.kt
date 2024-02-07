package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

/**
 * @author Hannah Schellekens
 */
interface ReferencedFileSetService {

    companion object {

        @JvmStatic
        fun getInstance(): ReferencedFileSetService {
            return ApplicationManager.getApplication().getService(ReferencedFileSetService::class.java)
        }
    }

    /**
     * [findReferencedFileSetWithoutCache], but then with cached values.
     */
    fun referencedFileSetOf(psiFile: PsiFile): Set<PsiFile>

    fun rootFilesOf(psiFile: PsiFile): Set<PsiFile>

    /**
     * Invalidates the caches for the given file.
     */
    fun dropCaches(file: VirtualFile)

    fun dropAllCaches()
}