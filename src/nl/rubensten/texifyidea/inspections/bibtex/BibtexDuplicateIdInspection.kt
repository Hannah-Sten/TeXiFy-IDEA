package nl.rubensten.texifyidea.inspections.bibtex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.BibtexIdIndex
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.psi.BibtexId
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.*

/**
 * @author Ruben Schellekens
 */
open class BibtexDuplicateIdInspection : TexifyInspectionBase() {

    override fun getInspectionGroup() = InsightGroup.BIBTEX

    override fun getDisplayName() = "Duplicate ID"

    override fun getInspectionId() = "DuplicateId"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        // Contains all the \bibitem commands in the file set.
        val bibitems = TexifyUtil.findLabels(file.commandsInFileSet()).asSequence()
                .filter { it is LatexCommands && it.name == "\\bibitem" }
                .mapNotNull { (it as LatexCommands).requiredParameter(0) }
                .toSet()

        // All the ids that have been defined in the bibtex file. And next a list of all names.
        val bibtexIds = BibtexIdIndex.getIndexedIdsInFileSet(file)
        val strings = bibtexIds.map { it.idName() }.toList()

        val added = HashSet<BibtexId>()
        for (bibtexId in bibtexIds) {
            val idName = bibtexId.idName()

            // Check if defined as bibitem.
            if (bibtexId !in added && idName in bibitems) {
                descriptors.add(manager.createProblemDescriptor(
                        bibtexId,
                        TextRange(0, bibtexId.textLength - 1),
                        "Duplicate identifier '$idName'",
                        ProblemHighlightType.GENERIC_ERROR,
                        isOntheFly
                ))
                added += bibtexId
                continue
            }

            // Check if defined in bibtex files.
            if (bibtexId !in added && strings.findAtLeast(2) { it == idName }) {
                descriptors.add(manager.createProblemDescriptor(
                        bibtexId,
                        TextRange(0, bibtexId.textLength - 1),
                        "Duplicate identifier '$idName'",
                        ProblemHighlightType.GENERIC_ERROR,
                        isOntheFly
                ))
                added += bibtexId
            }
        }

        return descriptors
    }
}