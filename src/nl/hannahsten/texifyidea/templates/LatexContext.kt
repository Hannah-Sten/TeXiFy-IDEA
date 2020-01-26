package nl.hannahsten.texifyidea.templates

import com.intellij.codeInsight.template.EverywhereContextType
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.util.inMathContext

/**
 * Defines a LaTeX template context, used to define in which context
 * a live template is available.
 *
 * @author Abby Berkers
 */
open class LatexContext(
        id: String,
        name: String,
        baseContextType: Class<out TemplateContextType>
) : TemplateContextType(id, name, baseContextType) {

    override fun isInContext(file: PsiFile, offset: Int): Boolean =
            file is LatexFile


    class Generic : LatexContext("LATEX", "LaTeX", EverywhereContextType::class.java)

    open class LatexMathContext : LatexContext("LATEX_MATH", "Math", Generic::class.java) {

        override fun isInContext(file: PsiFile, offset: Int): Boolean =
                file is LatexFile && file.findElementAt(offset)?.inMathContext() == true

    }
}
