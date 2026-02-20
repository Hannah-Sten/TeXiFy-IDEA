package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.text.StringUtil
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.LatexReferenceInsertHandler
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.remotelibraries.RemoteLibraryManager
import nl.hannahsten.texifyidea.util.labels.findBibtexItems
import nl.hannahsten.texifyidea.util.parser.getAuthors
import nl.hannahsten.texifyidea.util.parser.getIdentifier
import nl.hannahsten.texifyidea.util.parser.getTitle

object LatexBibliographyReferenceProvider : LatexContextAgnosticCompletionProvider() {

    override fun addCompletions(parameters: CompletionParameters, result: CompletionResultSet) {
        val localEntries = parameters.originalFile.findBibtexItems()
        val localBibtexIds = localEntries.filterIsInstance<BibtexEntry>().mapNotNull { it.id }

        // Add the remote entries to the autocompletion, only adding the entries that do not exist in the local bibliography yet.
        val remoteEntries = RemoteLibraryManager.getInstance().getLibraries()
            .flatMap { it.value.entries }
            .filter { it.id !in localBibtexIds }

        // Make sure the project makes sense (#3659)
        val lookupItems = localEntries.filter { it.project == it.containingFile.project }.mapNotNull { bibtexEntry ->
            when (bibtexEntry) {
                is BibtexEntry -> createLookupElementFromBibtexEntry(bibtexEntry)
                is LatexCommands -> createLookupElementFromLatexCommand(bibtexEntry)
                else -> null
            }
        }

        val lookupItemsFromRemote = remoteEntries.mapNotNull { createLookupElementFromBibtexEntry(it, remote = true) }

        val before = result.prefixMatcher.prefix
        val prefix = if (before.contains(',')) before.substring(before.lastIndexOf(',') + 1) else before
        result.withPrefixMatcher(CamelHumpMatcher(prefix, false)).addAllElements(lookupItems + lookupItemsFromRemote)
    }

    private fun createLookupElementFromBibtexEntry(bibtexEntry: BibtexEntry, remote: Boolean = false): LookupElementBuilder? {
        // Ensure a consistent project (#3802)
        if (bibtexEntry.id?.project != bibtexEntry.id?.containingFile?.project) return null

        val lookupStrings = bibtexEntry.getAuthors() + bibtexEntry.getTitle()
        val icon = if (remote) AllIcons.ObjectBrowser.ShowLibraryContents else TexifyIcons.DOT_BIB

        return LookupElementBuilder.create(bibtexEntry.getIdentifier())
            .withPsiElement(bibtexEntry.id)
            .withPresentableText(bibtexEntry.getTitle())
            .bold()
            .withInsertHandler(LatexReferenceInsertHandler(remote, if (remote) bibtexEntry else null))
            .withLookupStrings(lookupStrings)
            .withTypeText(bibtexEntry.getIdentifier(), true)
            .withIcon(icon)
    }

    private fun createLookupElementFromLatexCommand(latexCommand: LatexCommands): LookupElementBuilder? {
        if (latexCommand.requiredParametersText().isEmpty()) return null
        return LookupElementBuilder.create(latexCommand.requiredParametersText()[0])
            .bold()
            .withInsertHandler(LatexReferenceInsertHandler())
            .withTypeText(latexCommand.containingFile.name + ": " + (1 + StringUtil.offsetToLineNumber(latexCommand.containingFile.text, latexCommand.textOffset)), true)
            .withIcon(TexifyIcons.DOT_BIB)
    }
}