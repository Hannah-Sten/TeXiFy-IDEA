package nl.hannahsten.texifyidea.templates

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.util.inMathContext
import nl.hannahsten.texifyidea.util.inVerbatim

/**
 * Defines a LaTeX template context, used to define in which context
 * a live template is available.
 *
 * @author Abby Berkers
 */
open class LatexContext(name: String) : TemplateContextType(name) {

    override fun isInContext(context: TemplateActionContext): Boolean = context.file is LatexFile

    class Generic : LatexContext("LaTeX") {

        override fun isInContext(context: TemplateActionContext): Boolean =
            context.file is LatexFile && context.file.findElementAt(context.startOffset)?.inVerbatim() == false
    }

    open class LatexMathContext : LatexContext("Math") {

        override fun isInContext(context: TemplateActionContext): Boolean =
            context.file is LatexFile && context.file.findElementAt(context.startOffset)?.inMathContext() == true
    }
}
