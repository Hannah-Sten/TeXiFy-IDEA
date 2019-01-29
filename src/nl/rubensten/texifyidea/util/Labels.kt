package nl.rubensten.texifyidea.util

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.BibtexIdIndex
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.psi.BibtexId
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.settings.TexifySettings
import kotlin.streams.toList

/**
 * Finds all the defined labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findLabelsInFileSet(): Set<String> {
    val bibtex = findBibtexLabelsInFileSet()
    val userSpecificLabels = findMyLabels()
    return (bibtex + userSpecificLabels).toSet()
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

fun PsiFile.wrapFindMyLabels(): Set<String> = findMyLabels().toSet()

fun PsiFile.findMyLabels(): Sequence<String> {
    val commands = TexifySettings.getInstance().labelCommands
            .mapKeys { addLeadingSlash(it.key) }
    return LatexCommandsIndex.getItemsInFileSet(this)
            .asSequence().filter { commands.containsKey(it.name) }
            .map { it.name to it.requiredParameters }
            .filter { it.second.isNotEmpty() }
            .mapNotNull {
                val position = commands[it.first]
                if (position != null && it.second.size >= position)
                    it.second[position - 1] else null
            }
}

fun addLeadingSlash(command: String): String {
    return if (command[0] == '\\') command else "\\" + command
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
 * Finds all the labels within the collection of commands.
 *
 * @return A collection of all label commands.
 */
fun Collection<PsiElement>.findLabels(): Collection<PsiElement> {
    val commandNames = TexifySettings.getInstance().labelCommands
            .mapKeys { addLeadingSlash(it.key) }
    return filter {
        if (it is LatexCommands) {
            commandNames.containsKey(it.name)
        } else true
    }
}

/**
 * Finds all defined labels within a given file.
 *
 * @param file
 *         The file to analyse the file set of.
 * @return The found label commands.
 */
fun PsiFile.findLabelingCommands(): Collection<PsiElement> {
    val commandNames = TexifySettings.getInstance().labelCommands
            .mapKeys { addLeadingSlash(it.key) }
    val commands = LatexCommandsIndex.getItems(this).asSequence()
            .filter { commandNames.containsKey(it.name) }
    val bibtexIds = BibtexIdIndex.getIndexedIds(this).asSequence()
            .filter { commandNames.containsKey(it.name) }
    return (commands + bibtexIds).toList()
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
fun Project.findLabels(key: String?): Collection<PsiElement> = findLabels().parallelStream()
        .filter { command ->
            if (command is LatexCommands) {
                val parameters = runReadAction { command.requiredParameters }
                parameters.isNotEmpty() && key != null && key == parameters.firstOrNull()

            }
            else if (command is BibtexId) {
                key == command.name
            }
            else false
        }
        .toList()