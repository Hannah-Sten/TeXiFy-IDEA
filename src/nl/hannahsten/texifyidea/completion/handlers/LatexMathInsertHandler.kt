package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement

/**
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexMathInsertHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        LatexCommandArgumentInsertHandler().handleInsert(context, item)
        LatexCommandPackageIncludeHandler().handleInsert(context, item)
        RightInsertHandler().handleInsert(context, item)
    }
}
