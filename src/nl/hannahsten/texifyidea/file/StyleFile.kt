package nl.hannahsten.texifyidea.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import nl.hannahsten.texifyidea.LatexLanguage

/**
 * @author Hannah Schellekens
 */
class StyleFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LatexLanguage) {

    override fun getFileType() = StyleFileType

    override fun toString() = "LaTeX style file"
}
