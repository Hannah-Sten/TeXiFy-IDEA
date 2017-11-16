package nl.rubensten.texifyidea.structure.bibtex

import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.structure.latex.LatexFilePresentation

/**
 * @author Ruben Schellekens
 */
open class BibtexFilePresentation(val file: PsiFile) : LatexFilePresentation(file) {

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_BIB!!
}