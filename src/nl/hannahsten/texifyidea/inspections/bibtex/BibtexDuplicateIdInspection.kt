package nl.hannahsten.texifyidea.inspections.bibtex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewBibtexEntryIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.util.parser.getIdentifier
import nl.hannahsten.texifyidea.util.parser.traverseTyped

/**
 * @author Hannah Schellekens
 */
open class BibtexDuplicateIdInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.BIBTEX

    override fun getDisplayName() = "Duplicate ID"

    override val inspectionId = "DuplicateId"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        val project = file.project
        val fileset = LatexProjectStructure.getFilesetScopeFor(file)
        for (bibtexEntry in file.traverseTyped<BibtexEntry>()) {
            val idName = bibtexEntry.getIdentifier()
            if(NewBibtexEntryIndex.countByName(idName, project, fileset) > 1) {
                descriptors.add(
                    manager.createProblemDescriptor(
                        bibtexEntry,
                        TextRange(0, bibtexEntry.textLength - 1),
                        "Duplicate identifier '$idName'",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                    )
                )
            }
        }

        return descriptors
    }
}
