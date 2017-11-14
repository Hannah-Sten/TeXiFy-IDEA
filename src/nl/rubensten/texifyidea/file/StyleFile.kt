package nl.rubensten.texifyidea.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import nl.rubensten.texifyidea.LatexLanguage

/**
 * @author Ruben Schellekens
 */
class StyleFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LatexLanguage.INSTANCE) {

    override fun getFileType() = StyleFileType

    override fun toString() = "LaTeX style file"
}
