package nl.hannahsten.texifyidea.templates

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.util.parser.inMathContext
import nl.hannahsten.texifyidea.util.parser.inVerbatim

/**
 * Defines a LaTeX template context, used to define in which context
 * a live template is available.
 *
 * @author Abby Berkers
 */
open class TemplateLatexContext(name: String) : TemplateContextType(name) {

    override fun isInContext(context: TemplateActionContext): Boolean = context.file is LatexFile

    class TemplateLatexGeneric : TemplateLatexContext("LaTeX") {
        override fun isInContext(context: TemplateActionContext): Boolean =
            context.file is LatexFile && context.file.findElementAt(context.startOffset)?.inVerbatim() == false
    }

    open class TemplateLatexMathContext : TemplateLatexContext("Math") {

        override fun isInContext(context: TemplateActionContext): Boolean =
            context.file is LatexFile && context.file.findElementAt(context.startOffset)?.inMathContext() == true
    }
}
