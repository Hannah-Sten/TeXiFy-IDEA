package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder

/**
 * @author Hannah Schellekens
 */
open class CompositeHandler<T : LookupElement>(vararg val handlers: InsertHandler<T>) : InsertHandler<T> {

    override fun handleInsert(context: InsertionContext, lookupElement: T) {
        handlers.forEach { it.handleInsert(context, lookupElement) }
    }
}

/**
 * Adds multiple insert handlers to the LookupElementBuilder.
 */
fun LookupElementBuilder.withInsertHandlers(vararg handlers: InsertHandler<LookupElement>): LookupElementBuilder = withInsertHandler(CompositeHandler(*handlers))