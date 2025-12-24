package nl.hannahsten.texifyidea.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceBase
import nl.hannahsten.texifyidea.util.files.findVirtualFileByAbsoluteOrRelativePath
import nl.hannahsten.texifyidea.util.files.psiFile

/**
 * Assuming the given PsiElement contains the filePath (absolute or relative), this is a reference to the file.
 * If textRange is given that will be used, otherwise the range of filePath will be used.
 */
class SimpleFileReference(fileReference: PsiElement, val filePath: String, val textRange: TextRange? = null) : PsiReferenceBase<PsiElement>(fileReference) {

    init {
        rangeInElement = textRange ?: TextRange.from(element.text.indexOf(filePath), filePath.length)
    }

    override fun resolve(): PsiFile? = element.containingFile
        ?.containingDirectory
        ?.virtualFile
        ?.findVirtualFileByAbsoluteOrRelativePath(filePath)
        ?.psiFile(element.project)
}