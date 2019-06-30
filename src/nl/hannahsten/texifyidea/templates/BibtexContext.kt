package nl.hannahsten.texifyidea.templates

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.BibtexFile

/**
 * @author Hannah Schellekens
 */
open class BibtexContext : TemplateContextType("BIBTEX", "BibTeX") {

    override fun isInContext(file: PsiFile, offset: Int) = file is BibtexFile
}