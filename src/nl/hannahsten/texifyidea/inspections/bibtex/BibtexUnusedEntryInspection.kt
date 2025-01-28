package nl.hannahsten.texifyidea.inspections.bibtex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.createSmartPointer
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.SafeDeleteFix
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.BibtexId
import nl.hannahsten.texifyidea.util.parser.childrenOfType

class BibtexUnusedEntryInspection : TexifyInspectionBase() {

    override val inspectionGroup: InsightGroup = InsightGroup.BIBTEX

    override val inspectionId: String = "UnusedEntry"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> =
        file.childrenOfType(BibtexId::class)
            .asSequence()
            .filter { ReferencesSearch.search(it).toList().isEmpty() }
            .map {
                manager.createProblemDescriptor(
                    it,
                    "Bibtex entry is not used",
                    RemoveBibtexEntryFix(it.createSmartPointer()),
                    ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                    isOntheFly
                )
            }
            .toList()

    class RemoveBibtexEntryFix(private val id: SmartPsiElementPointer<BibtexId>) : SafeDeleteFix(id.element as PsiElement) {

        override fun getText(): String = "Safe delete ${id.element?.text}"
    }
}