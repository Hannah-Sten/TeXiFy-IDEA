package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import nl.hannahsten.texifyidea.lang.commands.Argument

/**
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexMathInsertHandler(val arguments: List<Argument>?) : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        LatexCommandArgumentInsertHandler(arguments).handleInsert(context, item)
        LatexCommandPackageIncludeHandler().handleInsert(context, item)
        RightInsertHandler().handleInsert(context, item)
    }
}
