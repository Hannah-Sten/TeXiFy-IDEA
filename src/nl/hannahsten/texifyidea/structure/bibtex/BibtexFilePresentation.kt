package nl.hannahsten.texifyidea.structure.bibtex

import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.structure.latex.LatexFilePresentation

/**
 * @author Hannah Schellekens
 */
open class BibtexFilePresentation(val file: PsiFile) : LatexFilePresentation(file) {

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_BIB!!
}