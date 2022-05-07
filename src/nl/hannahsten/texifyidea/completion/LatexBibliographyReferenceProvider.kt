package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.LatexReferenceInsertHandler
import nl.hannahsten.texifyidea.externallibrary.RemoteLibraryManager
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.labels.findBibtexItems
import java.util.*

object LatexBibliographyReferenceProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val remoteEntries = RemoteLibraryManager.getInstance().libraries.flatMap { it.value }
        val localEntries = parameters.originalFile.findBibtexItems()

        val lookupItems = (localEntries + remoteEntries)
            .mapNotNull { bibtexEntry ->
                when (bibtexEntry) {
                    is BibtexEntry -> createLookupElementFromBibtexEntry(bibtexEntry)
                    is LatexCommands -> createLookupElementFromLatexCommand(bibtexEntry)
                    else -> null
                }
            }
        val before = result.prefixMatcher.prefix
        val prefix =
            if (before.contains(',')) {
                before.substring(before.lastIndexOf(',') + 1)
            }
            else {
                before
            }
        result.withPrefixMatcher(CamelHumpMatcher(prefix, false)).addAllElements(lookupItems)
    }

    private fun createLookupElementFromBibtexEntry(bibtexEntry: BibtexEntry): LookupElementBuilder {
        val lookupStrings = LinkedList(bibtexEntry.authors)
        lookupStrings.add(bibtexEntry.title)
        return LookupElementBuilder.create(bibtexEntry.identifier)
            .withPsiElement(bibtexEntry.id)
            .withPresentableText(bibtexEntry.title)
            .bold()
            .withInsertHandler(LatexReferenceInsertHandler())
            .withLookupStrings(lookupStrings)
            .withTypeText(
                bibtexEntry.identifier,
                true
            )
            .withIcon(TexifyIcons.DOT_BIB)
    }

    private fun createLookupElementFromLatexCommand(latexCommand: LatexCommands): LookupElementBuilder? {
        if (latexCommand.requiredParameters.isEmpty()) null
        return LookupElementBuilder.create(latexCommand.requiredParameters[0])
            .bold()
            .withInsertHandler(LatexReferenceInsertHandler())
            .withTypeText(latexCommand.containingFile.name + ": " + (1 + StringUtil.offsetToLineNumber(latexCommand.containingFile.text, latexCommand.textOffset)), true)
            .withIcon(TexifyIcons.DOT_BIB)
    }
}