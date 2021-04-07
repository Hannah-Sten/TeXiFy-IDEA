package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.util.magic.PatternMagic

/**
 * @author Hannah Schellekens
 */
open class LatexFilePresentation(private val file: PsiFile) : ItemPresentation {

    private val presentableText = PatternMagic.fileExtension.matcher(file.name).replaceAll("")

    override fun getPresentableText() = presentableText!!

    override fun getLocationString() = file.virtualFile.path

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_LATEX
}