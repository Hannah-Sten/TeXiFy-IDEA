package nl.hannahsten.texifyidea.inspections.bibtex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import com.intellij.psi.search.searches.ReferencesSearch
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.BibtexId
import nl.hannahsten.texifyidea.util.childrenOfType

class BibtexUnusedEntryInspection : TexifyInspectionBase() {
    override val inspectionGroup: InsightGroup = InsightGroup.BIBTEX

    override val inspectionId: String = "UnusedEntry"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> =
        file.childrenOfType(BibtexId::class)
            .filter { ReferencesSearch.search(it).toList().isEmpty() }
            .map {
                manager.createProblemDescriptor(
                    it,
                    "Bibtex entry is not used",
                    isOntheFly,
                    emptyArray(),
                    ProblemHighlightType.LIKE_UNUSED_SYMBOL
                )
            }
}