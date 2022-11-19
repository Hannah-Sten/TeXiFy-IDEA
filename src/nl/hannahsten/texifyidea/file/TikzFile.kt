package nl.hannahsten.texifyidea.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import nl.hannahsten.texifyidea.grammar.LatexLanguage

/**
 * @author Thomas Schouten
 */
open class TikzFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LatexLanguage) {

    override fun getFileType() = TikzFileType

    override fun toString() = "TikZ picture file"
}