package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement

/**
 * @author Hannah Schellekens
 */
open class CompositeHandler<T : LookupElement>(vararg val handlers: InsertHandler<T>) : InsertHandler<T> {

    override fun handleInsert(context: InsertionContext, lookupElement: T) {
        handlers.forEach { it.handleInsert(context, lookupElement) }
    }
}