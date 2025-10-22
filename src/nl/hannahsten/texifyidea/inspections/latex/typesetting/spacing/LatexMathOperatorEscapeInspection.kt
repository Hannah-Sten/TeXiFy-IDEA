package nl.hannahsten.texifyidea.inspections.latex.typesetting.spacing

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.inspections.AbstractTexifyContextAwareInspection
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import org.jetbrains.annotations.Nls

/**
 * Detects non-escaped common math functions like *sin*, *cos* and replaces them
 * with `\sin`, `\cos`.
 *
 * @author Sten Wessel
 */
class LatexMathOperatorEscapeInspection : AbstractTexifyContextAwareInspection(
    inspectionId = "MathOperatorEscape",
    inspectionGroup = InsightGroup.LATEX,
    applicableContexts = setOf(LatexContexts.Math)
) {

    private val mathOperatorsNames = CommandMagic.mathOperators.map { it.name }.toSet()

    override fun inspectElement(element: PsiElement, contexts: LContextSet, bundle: DefinitionBundle, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        if(element.node.elementType != LatexTypes.NORMAL_TEXT_WORD) return
        if(!isApplicableInContexts(contexts)) return
        if(element.text !in mathOperatorsNames) return
        // no need to check \text{}, as that is not math context
        descriptors.add(
            manager.createProblemDescriptor(
                element,
                "Non-escaped math operator",
                EscapeMathOperatorFix(),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly
            )
        )
    }

    /**
     * @author Sten Wessel
     */
    private class EscapeMathOperatorFix : LocalQuickFix {

        @Nls
        override fun getFamilyName(): String = "Escape math operator"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement
            val document = PsiDocumentManager.getInstance(project).getDocument(element.containingFile)
            document?.insertString(element.textOffset, "\\")
        }
    }
}