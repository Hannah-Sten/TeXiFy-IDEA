package nl.rubensten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.BibtexIdIndex
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.psi.BibtexId
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.settings.TexifySettings

/**
 * Finds all the defined labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findLabelsInFileSet(): Set<String> = (findAllLabelsInFileSet() + findBibtexLabelsInFileSet()).toSet()

/**
 * Finds all the defined latex labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findAllLabelsInFileSet(): Sequence<String> {
    val commands = TexifySettings.getInstance().getLabelCommandsLeadingSlash()
    return findLabelingCommandsSequence()
            .map { it.name to it.requiredParameters }
            .filter { it.second.isNotEmpty() }
            .mapNotNull {
                val position = commands[it.first]
                if (position != null && it.second.size >= position)
                    it.second[position - 1] else null
            }.filterNot { it == "" }
}

/**
 * Finds all the defined bibtex labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findBibtexLabelsInFileSet(): Sequence<String> = BibtexIdIndex.getIndexedIdsInFileSet(this)
        .asSequence()
        .map { it.text.substringEnd(1) }

/**
 * Finds all the labeling commands within the collection of commands.
 *
 * @return A collection of all label commands.
 */
fun Collection<PsiElement>.findLabels(): Collection<PsiElement> {
    val commandNames = TexifySettings.getInstance().getLabelCommandsLeadingSlash()
    return filter {
        if (it is LatexCommands) {
            commandNames.containsKey(it.name)
        }
        else {
            true
        }
    }
}

/**
 * Finds all defined labels within a given file.
 *
 * @param file
 *         The file to analyse the file set of.
 * @return The found label commands.
 */
fun PsiFile.findLabelingCommands(): Collection<LatexCommands> = findLabelingCommandsSequence().toList()

/**
 * make a sequence of all commands which specify an label
 */
fun PsiFile.findLabelingCommandsSequence(): Sequence<LatexCommands> {
    val commandNames = TexifySettings.getInstance().getLabelCommandsLeadingSlash()

    return LatexCommandsIndex.getItemsInFileSet(this).asSequence()
            .filter { commandNames.containsKey(it.name) }
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
 * Finds all specified bibtex entries
 */
fun PsiFile.findBibtexItems(): Collection<BibtexId> = BibtexIdIndex.getIndexedIdsInFileSet(this)

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