package nl.hannahsten.texifyidea.inspections.bibtex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.BibtexEntryIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.findAtLeast
import nl.hannahsten.texifyidea.util.findLatexCommandsLabels
import nl.hannahsten.texifyidea.util.identifier
import nl.hannahsten.texifyidea.util.requiredParameter

/**
 * @author Hannah Schellekens
 */
open class BibtexDuplicateIdInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.BIBTEX

    override fun getDisplayName() = "Duplicate ID"

    override val inspectionId = "DuplicateId"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        // Contains all the \bibitem commands in the file set.
        val bibitems = file.commandsInFileSet().asSequence().findLatexCommandsLabels(file.project)
            .filter { it.name == "\\bibitem" }
            .mapNotNull { it.requiredParameter(0) }
            .toSet()

        // All the ids that have been defined in the bibtex file. And next a list of all names.
        val bibtexIds = BibtexEntryIndex.getIndexedEntriesInFileSet(file)
        val strings = bibtexIds.map { it.identifier() }.toList()

        val added = HashSet<BibtexEntry>()
        // Check the bibtexIds in the current file
        for (bibtexEntry in BibtexEntryIndex.getIndexedEntries(file)) {
            val idName = bibtexEntry.identifier()

            // Check if defined as bibitem.
            if (bibtexEntry !in added && idName in bibitems) {
                descriptors.add(
                    manager.createProblemDescriptor(
                        bibtexEntry,
                        TextRange(0, bibtexEntry.textLength - 1),
                        "Duplicate identifier '$idName'",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                    )
                )
                added += bibtexEntry
                continue
            }

            // Check if defined in bibtex files.
            if (bibtexEntry !in added && strings.findAtLeast(2) { it == idName }) {
                descriptors.add(
                    manager.createProblemDescriptor(
                        bibtexEntry,
                        TextRange(0, bibtexEntry.textLength - 1),
                        "Duplicate identifier '$idName'",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                    )
                )
                added += bibtexEntry
            }
        }

        return descriptors
    }
}
