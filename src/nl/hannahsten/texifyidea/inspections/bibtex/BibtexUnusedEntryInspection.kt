package nl.hannahsten.texifyidea.inspections.bibtex

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.suggested.createSmartPointer
import com.jetbrains.rd.util.remove
import nl.hannahsten.texifyidea.index.BibtexEntryIndex
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.BibtexId
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.nextSiblingOfType

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
                    RemoveBibtexEntryFix(it.createSmartPointer()),
                    ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                    isOntheFly
                )
            }

    class RemoveBibtexEntryFix(private val id: SmartPsiElementPointer<BibtexId>) : LocalQuickFix {
        override fun getFamilyName(): String = "Remove BibTeX entry ${id.element?.text}"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val text = id.element?.text ?: return

            val searchScope = GlobalSearchScope.fileScope(descriptor.psiElement.containingFile)
            BibtexEntryIndex.getEntryByName(text, project, searchScope).forEach {
                it.parent.node.removeChild(it.node)
            }
        }
    }
}