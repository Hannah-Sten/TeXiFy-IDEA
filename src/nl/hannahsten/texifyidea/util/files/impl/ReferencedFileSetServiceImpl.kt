package nl.hannahsten.texifyidea.util.files.impl

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.util.files.ReferencedFileSetCache
import nl.hannahsten.texifyidea.util.files.ReferencedFileSetService

/**
 * @author Hannah Schellekens
 */
class ReferencedFileSetServiceImpl : ReferencedFileSetService {

    private val cache = ReferencedFileSetCache()

    override fun referencedFileSetOf(psiFile: PsiFile, useIndexCache: Boolean) = cache.fileSetFor(psiFile, useIndexCache)

    override fun rootFilesOf(psiFile: PsiFile, useIndexCache: Boolean): Set<PsiFile> = cache.rootFilesFor(psiFile, useIndexCache)

    override fun dropCaches(file: VirtualFile) = cache.dropCaches(file)

    override fun dropAllCaches() = cache.dropAllCaches()

    override fun markCacheOutOfDate() = cache.markCacheOutOfDate()
}