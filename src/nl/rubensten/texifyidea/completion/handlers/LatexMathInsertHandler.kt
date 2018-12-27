package nl.rubensten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateSettings
import nl.rubensten.texifyidea.lang.LatexCommand

/**
 * @author Ruben Schellekens, Sten Wessel
 */
class LatexMathInsertHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val command = item.`object` as LatexCommand

        if (command.command == "frac") {
            insertFraction(context)
        } else {
            LatexCommandArgumentInsertHandler().handleInsert(context, item)
            LatexCommandPackageIncludeHandler().handleInsert(context, item)
            RightInsertHandler().handleInsert(context, item)
        }
    }

    /**
     * Inserts `MATH.frac` live template
     */
    private fun insertFraction(context: InsertionContext) {
        val templateSettings = TemplateSettings.getInstance()
        val template = templateSettings.getTemplateById("MATH.frac")

        val editor = context.editor
        val templateManager = TemplateManager.getInstance(context.project)
        templateManager.startTemplate(editor, template)
    }
}
