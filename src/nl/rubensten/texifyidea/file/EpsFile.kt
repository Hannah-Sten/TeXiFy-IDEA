package nl.rubensten.texifyidea.file

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import nl.rubensten.texifyidea.LatexLanguage

class EpsFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LatexLanguage.INSTANCE) {
    override fun getFileType(): FileType = EpsFileType

    override fun toString(): String = "eps image: $name"
}
