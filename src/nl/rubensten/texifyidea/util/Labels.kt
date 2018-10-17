package nl.rubensten.texifyidea.util

import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.BibtexIdIndex
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.psi.LatexCommands

/**
 * Finds all the defined labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findLabelsInFileSet(): Set<String> {
    val latex = findLatexLabelsInFileSet()
    val bibtex = findBibtexLabelsInFileSet()
    return (latex + bibtex).toSet()
}

/**
 * Finds all the defined latex labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findLatexLabelsInFileSet(): Sequence<String> = LatexCommandsIndex.getItemsInFileSet(this)
        .asSequence()
        .filter { "\\label" == it.name || "\\bibitem" == it.name }
        .mapNotNull(LatexCommands::getRequiredParameters)
        .filter { it.isNotEmpty() }
        .map { it.first() }

/**
 * Finds all the defined bibtex labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findBibtexLabelsInFileSet(): Sequence<String> = BibtexIdIndex.getIndexedIdsInFileSet(this)
        .asSequence()
        .map { it.text.substringEnd(1) }