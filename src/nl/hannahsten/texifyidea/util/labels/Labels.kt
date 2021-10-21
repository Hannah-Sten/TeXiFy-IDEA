package nl.hannahsten.texifyidea.util.labels

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexParameterLabeledCommandsIndex
import nl.hannahsten.texifyidea.index.LatexParameterLabeledEnvironmentsIndex
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.files.psiFile

/**
 * Finds all the defined labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findLatexAndBibtexLabelStringsInFileSet(): Set<String> =
    (findLatexLabelStringsInFileSetAsSequence() + findBibtexLabelsInFileSetAsSequence()).toSet()

/**
 * Finds all the defined latex labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findLatexLabelStringsInFileSetAsSequence(): Sequence<String> {
    return findLatexLabelingElementsInFileSet().map { it.extractLabelName() }
}

/**
 * All labels in this file.
 */
fun PsiFile.findLatexLabelingElementsInFile(): Sequence<PsiElement> = sequenceOf(
    findLabelingCommandsInFile(),
    LatexParameterLabeledEnvironmentsIndex.getItems(this).asSequence(),
    LatexParameterLabeledCommandsIndex.getItems(this).asSequence()
).flatten()

/**
 * All labels in the fileset.
 */
fun PsiFile.findLatexLabelingElementsInFileSet(): Sequence<PsiElement> = sequenceOf(
    findLabelingCommandsInFileSet(),
    LatexParameterLabeledEnvironmentsIndex.getItemsInFileSet(this).asSequence(),
    LatexParameterLabeledCommandsIndex.getItemsInFileSet(this).asSequence()
).flatten()

/**
 * Make a sequence of all commands in the file set that specify a label. This does not include commands which define a label via an
 * optional parameter.
 */
fun PsiFile.findLabelingCommandsInFileSet(): Sequence<LatexCommands> {
    // If using the xr package to include label definitions in external files, include those external files when searching for labeling commands in the fileset
    val externalCommands = this.findXrPackageExternalDocuments().flatMap { it.commandsInFileSet() }
    return (this.commandsInFileSet() + externalCommands).asSequence().findLatexCommandsLabels(this.project)
}

/**
 * Find external files which contain label definitions, as used by the xr package, which are called with \externaldocument anywhere in the fileset.
 */
fun PsiFile.findXrPackageExternalDocuments(): List<PsiFile> {
    return this.commandsInFileSet()
        .filter { it.name == LatexGenericRegularCommand.EXTERNALDOCUMENT.commandWithSlash }
        .flatMap { it.references.filterIsInstance<InputFileReference>() }
        .mapNotNull { it.findAnywhereInProject(it.key)?.psiFile(project) }
}

/**
 * @see [findLabelingCommandsInFileSet] but then only for commands in this file.
 */
fun PsiFile.findLabelingCommandsInFile(): Sequence<LatexCommands> {
    return this.commandsInFile().asSequence().findLatexCommandsLabels(this.project)
}

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
