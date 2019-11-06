package nl.hannahsten.texifyidea.util.files.impl

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.util.files.ReferencedFileSetCache
import nl.hannahsten.texifyidea.util.files.ReferencedFileSetService

/**
 * @author Hannah Schellekens
 */
class ReferencedFileSetServiceImpl(project: Project) : ReferencedFileSetService {

    private val cache = ReferencedFileSetCache(project)

    override fun referencedFileSetOf(psiFile: PsiFile) = cache.fileSetFor(psiFile)

    override fun dropCaches(psiFile: PsiFile) = cache.dropCaches(psiFile)
}