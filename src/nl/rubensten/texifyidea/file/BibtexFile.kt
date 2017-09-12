package nl.rubensten.texifyidea.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import nl.rubensten.texifyidea.BibtexLanguage

/**
 * @author Ruben Schellekens
 */
open class BibtexFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, BibtexLanguage) {

    override fun getFileType() = BibtexFileType

    override fun toString() = "BibTeX bibliography file"
}