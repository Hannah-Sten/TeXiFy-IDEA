package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jetbrains.rd.util.first
import nl.hannahsten.texifyidea.index.BibtexEntryIndex
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.index.LatexParameterLabeledCommandsIndex
import nl.hannahsten.texifyidea.index.LatexParameterLabeledEnvironmentsIndex
import nl.hannahsten.texifyidea.lang.CommandManager
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic

/*
 * Collections
 */

/**
 * Finds all the defined labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findLatexAndBibtexLabelStringsInFileSet(): Set<String> =
    (findLatexLabelStringsInFileSetAsSequence() + findBibtexLabelsInFileSetAsSequence()).toSet()

/**
 * Finds all defined labels within a given file.
 *
 * @receiver The file to analyse the file set of.
 * @return The found label commands.
 */
fun PsiFile.findLabelsInFileSetAsCollection(): List<PsiElement> = sequenceOf(
    findLabelingCommandsInFileSetAsSequence(),
    LatexParameterLabeledEnvironmentsIndex.getItemsInFileSet(this).asSequence(),
    LatexParameterLabeledCommandsIndex.getItemsInFileSet(this).asSequence()
).flatten().toList()

/*
 * Sequences
 */

/**
 * Finds all the defined latex labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findLatexLabelStringsInFileSetAsSequence(): Sequence<String> {
    return findLatexLabelPsiElementsInFileSetAsSequence().map { it.extractLabelName() }
}

/**
 * All labels in this file.
 */
fun PsiFile.findLatexLabelPsiElementsInFileAsSequence(): Sequence<PsiElement> = sequenceOf(
    findLabelingCommandsInFileAsSequence(),
    LatexParameterLabeledEnvironmentsIndex.getItems(this).asSequence(),
    LatexParameterLabeledCommandsIndex.getItems(this).asSequence()
).flatten()

/**
 * All labels in the fileset.
 */
fun PsiFile.findLatexLabelPsiElementsInFileSetAsSequence(): Sequence<PsiElement> = sequenceOf(
    findLabelingCommandsInFileSetAsSequence(),
    LatexParameterLabeledEnvironmentsIndex.getItemsInFileSet(this).asSequence(),
    LatexParameterLabeledCommandsIndex.getItemsInFileSet(this).asSequence()
).flatten()

/**
 * Make a sequence of all commands in the file set that specify a label. This does not include commands which define a label via an
 * optional parameter.
 */
fun PsiFile.findLabelingCommandsInFileSetAsSequence(): Sequence<LatexCommands> {
    return this.commandsInFileSet().asSequence().findLatexCommandsLabels(this.project)
}

/**
 * @see [findLabelingCommandsInFileSetAsSequence] but then only for commands in this file.
 */
fun PsiFile.findLabelingCommandsInFileAsSequence(): Sequence<LatexCommands> {
    return this.commandsInFile().asSequence().findLatexCommandsLabels(this.project)
}

/*
 * Bibtex
 */

/**
 * Finds all the defined bibtex labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
private fun PsiFile.findBibtexLabelsInFileSetAsSequence(): Sequence<String> = findBibtexItems().asSequence()
    .mapNotNull {
        when (it) {
            is BibtexEntry -> it.name
            is LatexCommands -> it.requiredParameter(0)
            else -> null
        }
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

/*
 * Filtering sequence or collection
 */

/**
 * Finds all the labeling commands within the collection of PsiElements.
 *
 * @return A collection of all label commands.
 */
fun Collection<PsiElement>.findLatexCommandsLabels(project: Project): Collection<LatexCommands> {
    val commandNames = project.getLabelDefinitionCommands()
    return filterIsInstance<LatexCommands>().filter { commandNames.contains(it.name) }
}

/**
 * Finds all the labeling commands within the sequence of PsiElements.
 *
 * @return A sequence of all label commands.
 */
fun Sequence<PsiElement>.findLatexCommandsLabels(project: Project): Sequence<LatexCommands> {
    val commandNames = project.getLabelDefinitionCommands()
    return filterIsInstance<LatexCommands>().filter { commandNames.contains(it.name) }
}

/*
 * Project
 */

/**
 * Finds all defined labels within the project, including bibtex entries.
 *
 * @return The found label commands.
 */
fun Project.findAllLabelsAndBibtexIds(): Collection<PsiElement> {
    val commands = LatexCommandsIndex.getItems(this).findLatexCommandsLabels(this)
    val bibtexIds = BibtexEntryIndex.getIndexedEntries(this)
    val environments = LatexParameterLabeledEnvironmentsIndex.getItems(this)
    val parameterLabeledCommands = LatexParameterLabeledCommandsIndex.getItems(this)
    val result = ArrayList<PsiElement>(commands)
    result.addAll(bibtexIds)
    result.addAll(environments)
    result.addAll(parameterLabeledCommands)
    return result
}

/**
 * All commands that represent a reference to a label, including user defined commands.
 */
fun Project.getLabelReferenceCommands(): Set<String> {
    CommandManager.updateAliases(CommandMagic.labelReferenceWithoutCustomCommands, this)
    return CommandManager.getAliases(CommandMagic.labelReferenceWithoutCustomCommands.first())
}

/**
 * Get all commands defining labels, including user defined commands. This will not check if the aliases need to be updated.
 */
fun getLabelDefinitionCommands() = CommandManager.getAliases(CommandMagic.labelDefinitionsWithoutCustomCommands.first())

/**
 * Get all commands defining labels, including user defined commands.
 * If you need to know which parameters of user defined commands define a label, use [CommandManager.labelAliasesInfo].
 *
 * This will check if the cache of user defined commands needs to be updated, based on the given project, and therefore may take some time.
 */
fun Project.getLabelDefinitionCommands(): Set<String> {
    // Check if updates are needed
    CommandManager.updateAliases(CommandMagic.labelDefinitionsWithoutCustomCommands, this)
    return CommandManager.getAliases(CommandMagic.labelDefinitionsWithoutCustomCommands.first())
}

/*
 * Other utils
 */

/**
 * Extracts the label name from the PsiElement given that the PsiElement represents a label.
 */
fun PsiElement.extractLabelName(): String {
    return when (this) {
        is BibtexEntry -> identifier() ?: ""
        is LatexCommands -> {
            if (CommandMagic.labelAsParameter.contains(name)) {
                optionalParameterMap.toStringMap()["label"]!!
            }
            else {
                // For now just take the first label name (may be multiple for user defined commands)
                val info = CommandManager.labelAliasesInfo.getOrDefault(name, null)
                val position = info?.positions?.firstOrNull() ?: 0
                val prefix = info?.prefix ?: ""
                // Skip optional parameters for now (also below and in
                prefix + this.requiredParameter(position)
            }
        }
        is LatexEnvironment -> this.label ?: ""
        else -> text
    }
}

/**
 * Extracts the label element (so the element that should be resolved to) from the PsiElement given that the PsiElement represents a label.
 */
fun PsiElement.extractLabelElement(): PsiElement? {
    fun getLabelParameterText(command: LatexCommandWithParams): LatexParameterText {
        val optionalParameters = command.optionalParameterMap
        val labelEntry = optionalParameters.filter { pair -> pair.key.toString() == "label" }.first()
        val contentList = labelEntry.value.keyvalContentList
        return contentList.firstOrNull { c -> c.parameterText != null }?.parameterText
            ?: contentList.first { c -> c.parameterGroup != null }.parameterGroup!!.parameterGroupText!!.parameterTextList.first()
    }

    return when (this) {
        is BibtexEntry -> firstChildOfType(BibtexId::class)
        is LatexCommands -> {
            if (CommandMagic.labelAsParameter.contains(name)) {
                return getLabelParameterText(this)
            }
            else {
                // For now just take the first label name (may be multiple for user defined commands)
                val info = CommandManager.labelAliasesInfo.getOrDefault(name, null)
                val position = info?.positions?.firstOrNull() ?: 0

                // Skip optional parameters for now
                this.parameterList.mapNotNull { it.requiredParam }.getOrNull(position)
                    ?.firstChildOfType(LatexParameterText::class)
            }
        }
        is LatexEnvironment -> {
            if (EnvironmentMagic.labelAsParameter.contains(environmentName)) {
                getLabelParameterText(beginCommand)
            }
            else {
                null
            }
        }
        else -> null
    }
}

/**
 * Finds all section marker commands (as defined in [CommandMagic.sectionMarkers]) in the project.
 *
 * @return A list containing all the section marker [LatexCommands].
 */
fun Project.findSectionMarkers() = LatexCommandsIndex.getItems(this).filter {
    it.commandToken.text in CommandMagic.sectionMarkers
}
