package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import com.intellij.psi.search.searches.ReferencesSearch
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.SafeDeleteFix
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType
import nl.hannahsten.texifyidea.util.isFigureLabel
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import java.util.*

open class LatexFigureNotReferencedInspection : TexifyInspectionBase() {

    override val inspectionGroup: InsightGroup = InsightGroup.LATEX

    override val inspectionId: String = "FigureNotReferenced"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName(): String = "Figure not referenced"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val figureLabels = file.traverseCommands().filter { it.isFigureLabel() }
        val descriptors = descriptorList()
        for (label in figureLabels) {
            // Remove labels that are referenced in the file
            val labelText = label.findFirstChildTyped<LatexParameterText>() ?: continue
            if(ReferencesSearch.search(labelText).none()) {
                descriptors.add(createDescriptor(manager, label, isOntheFly) ?: continue)
            }
        }
        return descriptors
    }

    private fun createDescriptor(manager: InspectionManager, label: LatexCommands, isOntheFly: Boolean): ProblemDescriptor? =
        label.findFirstChildOfType(LatexParameterText::class)?.let {
            manager.createProblemDescriptor(
                it,
                "Figure is not referenced",
                RemoveFigureFix(it.createSmartPointer()),
                ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                isOntheFly
            )
        }

    class RemoveFigureFix(label: SmartPsiElementPointer<LatexParameterText>) : SafeDeleteFix(label.element as PsiElement) {

        override fun getText(): String = "Safe delete figure environment"
    }
}

private val LatexCommands.labelName: String?
    get() = requiredParameterText(0)

private val LatexCommands.referencedLabelNames: List<String>
    get() = requiredParameterText(0)?.split(",") ?: emptyList()