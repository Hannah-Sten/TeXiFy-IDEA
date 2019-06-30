package nl.hannahsten.texifyidea.templates

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.LatexFile

/**
 * @author Sten Wessel
 */
open class LatexContext : TemplateContextType("LATEX", "LaTeX") {

    override fun isInContext(file: PsiFile, offset: Int) = file is LatexFile
}