package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.inspections.AbstractTexifyCommandBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.nameWithoutSlash
import nl.hannahsten.texifyidea.psi.nextContextualSiblingIgnoreWhitespace
import nl.hannahsten.texifyidea.psi.prevContextualSiblingIgnoreWhitespace

class LatexLabelBeforeCaptionInspection : AbstractTexifyCommandBasedInspection(
    inspectionId = "LabelBeforeCaption"
) {
    override fun inspectCommand(command: LatexCommands, contexts: LContextSet, defBundle: DefinitionBundle, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        val name = command.nameWithoutSlash
        if (name != "caption") return // locate the caption command
        val previousSibling = command.prevContextualSiblingIgnoreWhitespace() ?: return
        if (previousSibling !is LatexCommands) return
        val previousName = previousSibling.nameWithoutSlash
        if (previousName != "label") return // locate the label command
        val problem = manager.createProblemDescriptor(
            previousSibling,
            "A label should come after the caption",
            SwapLabelAndCaptionQuickFix(),
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            isOnTheFly
        )
        descriptors.add(problem)
    }

    class SwapLabelAndCaptionQuickFix : LocalQuickFix {
        override fun getFamilyName(): @IntentionFamilyName String = "Swap label and caption"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val labelElement = descriptor.psiElement as? LatexCommands ?: return
            val captionElement = labelElement.nextContextualSiblingIgnoreWhitespace() as? LatexCommands ?: return
            if (captionElement.nameWithoutSlash != "caption") return
            // swap the two elements
            swapElements(project, labelElement, captionElement)
        }

        fun swapElements(project: Project, element1: PsiElement, element2: PsiElement) {
            if (PsiTreeUtil.isAncestor(element1, element2, false) || PsiTreeUtil.isAncestor(element2, element1, false)) {
                return
            }
            val parent1 = element1.parent ?: return
            val parent2 = element2.parent ?: return
            parent1.addBefore(element2, element1)
            element2.delete()
            parent2.addAfter(element1, element2)
            element1.delete()
        }
    }
}
