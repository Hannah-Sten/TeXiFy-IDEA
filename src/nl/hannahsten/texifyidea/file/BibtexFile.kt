package nl.hannahsten.texifyidea.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import nl.hannahsten.texifyidea.grammar.BibtexLanguage

/**
 * @author Hannah Schellekens
 */
open class BibtexFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, BibtexLanguage) {

    override fun getFileType() = BibtexFileType

    override fun toString() = "BibTeX bibliography file"
}