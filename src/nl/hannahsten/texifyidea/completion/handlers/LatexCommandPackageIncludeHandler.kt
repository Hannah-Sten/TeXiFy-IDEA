package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.util.insertUsepackage

/**
 * @author Hannah Schellekens
 */
class LatexCommandPackageIncludeHandler : InsertHandler<LookupElement> {

    override fun handleInsert(insertionContext: InsertionContext, item: LookupElement) {
        val command = item.`object` as LatexCommand
        insertionContext.file.insertUsepackage(command.dependency)
    }
}
