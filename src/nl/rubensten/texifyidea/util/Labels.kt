package nl.rubensten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.BibtexIdIndex
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.psi.BibtexId
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

/**
 * Finds all the labels within the collection of commands.
 *
 * @return A collection of all label commands.
 */
fun Collection<PsiElement>.findLabels(): Collection<PsiElement> {
    return filter {
        if (it is LatexCommands) {
            val name = it.name
            "\\bibitem" == name || "\\label" == name
        }
        else true
    }
}

/**
 * Finds all defined labels within a given file.
 *
 * @param file
 *         The file to analyse the file set of.
 * @return The found label commands.
 */
fun PsiFile.findLabels(): Collection<PsiElement> {
    val commands = LatexCommandsIndex.getItems(this)
    val bibtexIds = BibtexIdIndex.getIndexedIds(this)
    val result = ArrayList<PsiElement>(commands)
    result.addAll(bibtexIds)
    return result.findLabels()
}

/**
 * Finds all defined labels within the project.
 *
 * @return The found label commands.
 */
fun Project.findLabels(): Collection<PsiElement> {
    val commands = LatexCommandsIndex.getItems(this)
    val bibtexIds = BibtexIdIndex.getIndexedIds(this)
    val result = ArrayList<PsiElement>(commands)
    result.addAll(bibtexIds)
    return result.findLabels()
}

/**
 * Finds all defined labels within the project matching the label key/id.
 *
 * @param key
 *         Key to match the label with.
 * @return A list of matched label commands.
 */
fun Project.findLabels(key: String?): Collection<PsiElement> = findLabels().filter { it.extractLabelName() == key }

/**
 * Extracts the label name from the PsiElement given that the PsiElement represents a label.
 */
fun PsiElement.extractLabelName(): String = when (this) {
    is BibtexId -> idName()
    is LatexCommands -> requiredParameter(0) ?: ""
    else -> text
}