package nl.hannahsten.texifyidea.util.labels

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewBibtexEntryIndex
import nl.hannahsten.texifyidea.index.NewLabelsIndex
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.files.psiFile

/**
 * Finds all the defined labels in the fileset of the file.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findLatexAndBibtexLabelStringsInFileSet(): Set<String> {
    val fileset = LatexProjectStructure.buildFilesetScope(this)
    return NewLabelsIndex.getAllLabels(fileset) + findBibtexLabelsInFileSetAsSequence()
}

/**
 * Finds all the defined latex labels in the fileset of the file.
 * May contain duplicates.
 *
 * @return A set containing all labels that are defined in the fileset of the given file.
 */
fun PsiFile.findLatexLabelStringsInFileSetAsSequence(): Sequence<String> {
    val fileset = LatexProjectStructure.buildFilesetScope(this)
    return NewLabelsIndex.getAllLabels(fileset).asSequence()
//    val externalDocumentCommand = this.findExternalDocumentCommand()
//    return findLatexLabelingElementsInFileSet().map { it.extractLabelName(externalDocumentCommand) }
}

/**
 * All labels in the fileset.
 * May contain duplicates.
 */
fun PsiFile.findLatexLabelingElementsInFileSet(): Sequence<PsiElement> {
    // TODO: Better implementation
    val fileset = LatexProjectStructure.buildFilesetScope(this)
    return NewLabelsIndex.getAllLabels(fileset).asSequence().flatMap {
        NewLabelsIndex.getByName(it, fileset)
    }
}

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
    return this.traverseCommands().findLatexCommandsLabels(this.project)
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

object Labels {

    fun isDefinedLabelOrBibtexLabel(label: String, project: Project, scope: GlobalSearchScope): Boolean {
        return NewLabelsIndex.existsByName(label, project, scope) || NewBibtexEntryIndex.existsByName(label, project, scope)
    }

    fun getUniqueLabelName(originalLabel: String, file: PsiFile): String {
        val project = file.project
        val fileset = LatexProjectStructure.buildFilesetScope(file)
        var counter = 2
        var candidate = originalLabel
        while(isDefinedLabelOrBibtexLabel(candidate, project, fileset)) {
            candidate = originalLabel + counter
            counter++
        }
        return candidate
    }
}