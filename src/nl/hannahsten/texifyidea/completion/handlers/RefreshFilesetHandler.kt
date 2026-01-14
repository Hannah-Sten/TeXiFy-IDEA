package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import nl.hannahsten.texifyidea.index.LatexDefinitionService

object RefreshFilesetHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        // Refresh the fileset to ensure that the file structure is up-to-date.
        // This is particularly useful after inserting a file name or path.
        LatexDefinitionService.getInstance(context.project).requestRefresh()
    }
}