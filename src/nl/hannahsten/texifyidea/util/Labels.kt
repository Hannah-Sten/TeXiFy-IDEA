package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.BibtexEntryIndex
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.index.LatexParameterLabeledEnvironmentsIndex
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.commandsInFileSet

/**
 * Finds all the defined labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findLatexAndBibtexLabelsInFileSet(): Set<String> = (findLatexLabelStringsInFileSetAsSequence() + findBibtexLabelsInFileSetAsSequence()).toSet()

/**
 * Find all defined labels in the fileset.
 */
fun PsiFile.findLatexAndBibtexLabelsInFileSetAsSequence(): Sequence<LatexCommands> = findLabelingCommandsInFileSetAsSequence() + findBibitemCommands()

/**
 * Finds all the defined latex labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findLatexLabelStringsInFileSetAsSequence(): Sequence<String> {
    return sequenceOf(
            LatexParameterLabeledEnvironmentsIndex.getItems(this).asSequence().map { it.extractLabelName() },
            findLabelingCommandsInFileSetAsSequence()
                    .map {
                        it.extractLabelName()
                    }.filterNot { it == "" }).flatten()
}

/**
 * @see [findLabelsInFileSetAsCollection]
 */
fun PsiFile.findLatexLabelPsiElementsInFileAsSequence(): Sequence<PsiElement> = sequenceOf(findLabelingCommandsInFileAsSequence(),
        LatexParameterLabeledEnvironmentsIndex.getItemsInFileSet(this).asSequence()).flatten()

/**
 * Finds all the defined bibtex labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findBibtexLabelsInFileSetAsSequence(): Sequence<String> = findBibtexItems().asSequence()
        .mapNotNull {
            when (it) {
                is BibtexEntry -> it.name
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
fun PsiFile.findLabelsInFileSetAsCollection(): Collection<PsiElement> = sequenceOf(findLabelingCommandsInFileSetAsSequence(),
        LatexParameterLabeledEnvironmentsIndex.getItemsInFileSet(this).asSequence()).flatten().toList()

/**
 * Make a sequence of all commands in the file set that specify a label. This does not include commands which define a label via an
 * optional parameter.
 */
fun PsiFile.findLabelingCommandsInFileSetAsSequence(): Sequence<LatexCommands> {
    val commandNames = TexifySettings.getInstance().labelCommands

    return this.commandsInFileSet().asSequence()
            .filter { commandNames.containsKey(it.name) }
}

/**
 * @see [findLabelingCommandsInFileSetAsSequence] but then only for commands in this file.
 */
fun PsiFile.findLabelingCommandsInFileAsSequence(): Sequence<LatexCommands> {
    val commandNames = TexifySettings.getInstance().labelCommands

    return this.commandsInFile().asSequence()
            .filter { commandNames.containsKey(it.name) }
}

/**
 * Finds all defined labels within the project.
 *
 * @return The found label commands.
 */
fun Project.findLabels(): Collection<PsiElement> {
    val commands = LatexCommandsIndex.getItems(this).findLabels()
    val bibtexIds = BibtexEntryIndex.getIndexedEntries(this)
    val environments = LatexParameterLabeledEnvironmentsIndex.getItems(this)
    val result = ArrayList(commands)
    result.addAll(bibtexIds)
    result.addAll(environments)
    return result
}

/**
 * Finds all specified bibtex entries
 */
fun PsiFile.findBibtexItems(): Collection<PsiElement> {
    val bibtex = BibtexEntryIndex.getIndexedEntriesInFileSet(this)
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
    return when (this) {
        is BibtexEntry -> identifier() ?: ""
        is LatexCommands -> {
            val position =
                    TexifySettings
                            .getInstance()
                            .labelPreviousCommands
                            .getOrDefault(name, null)
                            ?.position ?: return ""
            this.requiredParameter(position - 1) ?: ""
        }
        is LatexEnvironment -> this.label ?: ""
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
