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
import nl.hannahsten.texifyidea.remotelibraries.RemoteLibraryManager
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.labels.findBibtexItems
import java.util.*

object LatexBibliographyReferenceProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val localEntries = parameters.originalFile.findBibtexItems()
        val remoteEntries = RemoteLibraryManager.getInstance().libraries
            .flatMap { it.value }
            .toSet()
            // Filter ids that are already included in the local bib entries.
            .filter { it.id !in localEntries.filterIsInstance<BibtexEntry>().map { bib -> bib.id } }

        val lookupItems = localEntries.mapNotNull { bibtexEntry ->
                when (bibtexEntry) {
                    is BibtexEntry -> createLookupElementFromBibtexEntry(bibtexEntry)
                    is LatexCommands -> createLookupElementFromLatexCommand(bibtexEntry)
                    else -> null
                }
            }

        val lookupItemsFromRemote = remoteEntries.map { createLookupElementFromBibtexEntry(it, true) }

        val before = result.prefixMatcher.prefix
        val prefix =
            if (before.contains(',')) {
                before.substring(before.lastIndexOf(',') + 1)
            }
            else {
                before
            }
        result.withPrefixMatcher(CamelHumpMatcher(prefix, false)).addAllElements(lookupItems + lookupItemsFromRemote)
    }

    private fun createLookupElementFromBibtexEntry(bibtexEntry: BibtexEntry, remote: Boolean = false): LookupElementBuilder {
        val lookupStrings = LinkedList(bibtexEntry.authors)
        lookupStrings.add(bibtexEntry.title)
        return LookupElementBuilder.create(bibtexEntry.identifier)
            .withPsiElement(bibtexEntry.id)
            .withPresentableText(bibtexEntry.title)
            .bold()
            .withInsertHandler(LatexReferenceInsertHandler(remote, if (remote) bibtexEntry else null))
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