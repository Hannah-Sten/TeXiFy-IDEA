package nl.rubensten.texifyidea.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import nl.rubensten.texifyidea.LatexLanguage

/**
 * @author Ruben Schellekens
 */
class ClassFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LatexLanguage.INSTANCE) {

    override fun getFileType() = ClassFileType

    override fun toString() = "LaTeX class file"
}