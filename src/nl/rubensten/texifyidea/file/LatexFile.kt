package nl.rubensten.texifyidea.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import nl.rubensten.texifyidea.LatexLanguage

/**
 * @author Sten Wessel
 */
class LatexFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LatexLanguage.INSTANCE) {

    override fun getFileType() = LatexFileType

    override fun toString() = "LaTeX source file"
}
