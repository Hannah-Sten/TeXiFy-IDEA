package nl.hannahsten.texifyidea.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import nl.hannahsten.texifyidea.LatexLanguage

/**
 * @author Sten Wessel
 */
class LatexFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LatexLanguage) {

    override fun getFileType() = LatexFileType

    override fun toString() = "LaTeX source file"
}
