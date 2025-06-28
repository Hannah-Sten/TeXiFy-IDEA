package nl.hannahsten.texifyidea.util.labels

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.BibtexEntryIndex
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.commandsInFileSet

/**
 * Finds all the defined bibtex labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findBibtexLabelsInFileSetAsSequence(): Sequence<String> = findBibtexItems().asSequence()
    .mapNotNull {
        when (it) {
            is BibtexEntry -> it.name
            is LatexCommands -> it.requiredParameterText(0)
            else -> null
        }
    }

/**
 * Finds all specified bibtex entries
 */
fun PsiFile.findBibtexItems(): Collection<PsiElement> {
    val bibtex = BibtexEntryIndex().getIndexedEntriesInFileSet(this)
    val bibitem = findBibitemCommands().toList()
    return (bibtex + bibitem)
}

/**
 * Finds all \\bibitem-commands in the document
 */
fun PsiFile.findBibitemCommands(): Sequence<LatexCommands> = this.commandsInFileSet().asSequence()
    .filter { it.name == "\\bibitem" }