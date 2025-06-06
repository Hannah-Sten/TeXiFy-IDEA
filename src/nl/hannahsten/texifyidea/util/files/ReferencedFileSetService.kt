package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
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
    fun referencedFileSetOf(psiFile: PsiFile, useIndexCache: Boolean = true): Set<PsiFile>

    fun rootFilesOf(psiFile: PsiFile, useIndexCache: Boolean = true): Set<PsiFile>

    fun dropAllCaches(project: Project)

    fun markCacheOutOfDate()
}