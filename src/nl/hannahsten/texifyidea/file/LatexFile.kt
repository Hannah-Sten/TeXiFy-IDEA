package nl.hannahsten.texifyidea.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import nl.hannahsten.texifyidea.grammar.LatexLanguage

/**
 * @author Sten Wessel
 */
class LatexFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LatexLanguage), PsiNameIdentifierOwner {

    override fun getFileType() = LatexFileType

    override fun toString() = "LaTeX source file"

    /**
     * Required for refactoring
     */
    override fun getNameIdentifier(): PsiElement {
        return this
    }
}
