package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexMathContent
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.util.Magic
import org.jetbrains.annotations.Nls

/**
 * Detects non-escaped common math functions like *sin*, *cos* and replaces them
 * with `\sin`, `\cos`.
 *
 * @author Sten Wessel
 */
class LatexMathOperatorEscapeInspection : TexifyInspectionBase() {
    override val inspectionGroup: InsightGroup
        get() = InsightGroup.LATEX

    @Nls
    override fun getDisplayName(): String {
        return "Non-escaped common math operators"
    }

    override val inspectionId: String
        get() = "MathOperatorEscape"

    override fun inspectFile(
        file: PsiFile, manager: InspectionManager,
        isOntheFly: Boolean
    ): List<ProblemDescriptor> {
        val descriptors = descriptorList()
        val pattern = PlatformPatterns.psiElement(LatexTypes.NORMAL_TEXT_WORD)
        val envs =
            PsiTreeUtil.findChildrenOfType(file, LatexMathContent::class.java)
        for (env in envs) {
            env.acceptChildren(object : PsiRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    ProgressManager.checkCanceled()
                    if (pattern.accepts(element)) {
                        if (Magic.Command.slashlessMathOperators.contains(element.text)) {
                            descriptors.add(
                                manager.createProblemDescriptor(
                                    element,
                                    "Non-escaped math operator",
                                    EscapeMathOperatorFix(),
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                    isOntheFly
                                )
                            )
                        }
                    }
                    else {
                        super.visitElement(element)
                    }
                }
            })
        }
        return descriptors
    }

    /**
     * @author Sten Wessel
     */
    private class EscapeMathOperatorFix : LocalQuickFix {
        @Nls
        override fun getFamilyName(): String {
            return "Escape math operator"
        }

        override fun applyFix(
            project: Project,
            descriptor: ProblemDescriptor
        ) {
            val element = descriptor.psiElement
            val document =
                PsiDocumentManager.getInstance(project).getDocument(element.containingFile)
            document?.insertString(element.textOffset, "\\")
        }
    }
}