package nl.rubensten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.util.Magic

/**
 * @author Ruben Schellekens
 */
open class LatexFilePresentation(private val file: PsiFile) : ItemPresentation {

    private val presentableText = Magic.Pattern.fileExtension.matcher(file.name).replaceAll("")

    override fun getPresentableText() = presentableText!!

    override fun getLocationString() = file.virtualFile.path

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_LATEX!!
}