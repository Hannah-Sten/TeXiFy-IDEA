package nl.hannahsten.texifyidea.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import nl.hannahsten.texifyidea.grammar.LatexLanguage

/**
 * @author Hannah Schellekens
 */
class ClassFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LatexLanguage) {

    override fun getFileType() = ClassFileType

    override fun toString() = "LaTeX class file"
}