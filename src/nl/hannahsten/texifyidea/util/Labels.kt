package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.BibtexIdIndex
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.psi.BibtexId
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.files.commandsInFileSet

/**
 * Finds all the defined labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findLabelsInFileSet(): Set<String> = (findAllLabelsInFileSet() + findBibtexLabelsInFileSet()).toSet()

/**
 * Find all defined labels in the fileset.
 */
fun PsiFile.findLabelsInFileSetSequence(): Sequence<LatexCommands> = findLabelingCommandsSequence() + findBibitemCommands()

/**
 * Finds all the defined latex labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findAllLabelsInFileSet(): Sequence<String> {
    val commands = TexifySettings.getInstance().labelCommands
    return findLabelingCommandsSequence()
            .map { it.name to it.requiredParameters }
            .filter { it.second.isNotEmpty() }
            .mapNotNull {
                val position = commands[it.first]?.position
                if (position != null && it.second.size >= position)
                    it.second[position - 1] else null
            }.filterNot { it == "" }
}

/**
 * Finds all the defined bibtex labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findBibtexLabelsInFileSet(): Sequence<String>  = findBibtexItems().asSequence()
            .mapNotNull {
                when (it) {
                    is BibtexId -> it.text.substringEnd(1)
                    is LatexCommands -> it.requiredParameter(0)
                    else -> null
                }
            }


/**
 * Finds all the labeling commands within the collection of commands.
 *
 * @return A collection of all label commands.
 */
fun Collection<PsiElement>.findLabels(): Collection<PsiElement> {
    val commandNames = TexifySettings.getInstance().labelCommands
    return filter {
        if (it is LatexCommands) {
            commandNames.containsKey(it.name)
        }
        else true
    }
}

/**
 * Finds all defined labels within a given file.
 *
 * @receiver The file to analyse the file set of.
 * @return The found label commands.
 */
fun PsiFile.findLabels(): Collection<PsiElement> = findLabelingCommandsSequence().toList()

/**
 * Make a sequence of all commands that specify a label.
 */
fun PsiFile.findLabelingCommandsSequence(): Sequence<LatexCommands> {
    val commandNames = TexifySettings.getInstance().labelCommands

    return this.commandsInFileSet().asSequence()
            .filter { commandNames.containsKey(it.name) }
}

/**
 * Finds all defined labels within the project.
 *
 * @return The found label commands.
 */
fun Project.findLabels(): Collection<PsiElement> {
    val commandNames = TexifySettings.getInstance().labelCommands

    val commands = LatexCommandsIndex.getItems(this)
    val commandsFiltered = commands.filter { commandNames.containsKey(it.name) }
    val bibtexIds = BibtexIdIndex.getIndexedIds(this)
    val result = ArrayList<PsiElement>(commandsFiltered)
    result.addAll(bibtexIds)
    return result.findLabels()
}

/**
 * Finds all specified bibtex entries
 */
fun PsiFile.findBibtexItems(): Collection<PsiElement> {
    val bibtex = BibtexIdIndex.getIndexedIdsInFileSet(this)
    val bibitem = findBibitemCommands().toList()
    return (bibtex + bibitem)
}

/**
 * Finds all \\bibitem-commands in the document
 */
fun PsiFile.findBibitemCommands(): Sequence<LatexCommands> = this.commandsInFileSet().asSequence()
        .filter { it.name == "\\bibitem" }

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
fun PsiElement.extractLabelName(): String {
    val labelingCommands = TexifySettings.getInstance().labelCommands
    return when (this) {
        is BibtexId -> idName()
        is LatexCommands -> {
            val position = labelingCommands[this.name]?.position ?: return ""
            this.requiredParameter(position - 1) ?: ""
        }
        else -> text
    }
}

/**
 * Finds all section marker commands (as defined in [Magic.Command.sectionMarkers]) in the project.
 *
 * @return A list containing all the section marker [LatexCommands].
 */
fun Project.findSectionMarkers() = LatexCommandsIndex.getItems(this).filter {
    it.commandToken.text in Magic.Command.sectionMarkers
}
