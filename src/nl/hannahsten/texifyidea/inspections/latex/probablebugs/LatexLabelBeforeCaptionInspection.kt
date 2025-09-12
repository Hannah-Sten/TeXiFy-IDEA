package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.inspections.AbstractTexifyCommandBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.nameWithoutSlash
import nl.hannahsten.texifyidea.psi.nextContextualSiblingIgnoreWhitespace
import nl.hannahsten.texifyidea.psi.prevContextualSiblingIgnoreWhitespace

class LatexLabelBeforeCaptionInspection : AbstractTexifyCommandBasedInspection(
    inspectionId = "LabelBeforeCaption"
) {
    override fun inspectCommand(command: LatexCommands, contexts: LContextSet, lookup: LatexSemanticsLookup, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
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
        override fun getFamilyName(): @IntentionFamilyName String {
            return "Swap label and caption"
        }

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
            val project = element1.project
            val documentManager = PsiDocumentManager.getInstance(project)
            val document = documentManager.getDocument(element1.containingFile)
                ?: throw IllegalStateException("Cannot access document")

            val text1 = element1.text
            val text2 = element2.text
            val range1 = element1.textRange
            val range2 = element2.textRange

            if (range1.startOffset < range2.startOffset) {
                document.replaceString(range2.startOffset, range2.endOffset, text1)
                document.replaceString(range1.startOffset, range1.endOffset, text2)
            }
            else {
                document.replaceString(range1.startOffset, range1.endOffset, text2)
                document.replaceString(range2.startOffset, range2.endOffset, text1)
            }
        }
    }
}
