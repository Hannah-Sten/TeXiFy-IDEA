package nl.hannahsten.texifyidea.templates

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import nl.hannahsten.texifyidea.file.BibtexFile

/**
 * @author Hannah Schellekens
 */
open class BibtexContext : TemplateContextType("BibTeX") {

    override fun isInContext(context: TemplateActionContext) = context.file is BibtexFile
}