package nl.rubensten.texifyidea.templates

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.file.BibtexFile

/**
 * @author Ruben Schellekens
 */
open class BibtexContext : TemplateContextType("BIBTEX", "BibTeX") {

    override fun isInContext(file: PsiFile, offset: Int) = file is BibtexFile
}