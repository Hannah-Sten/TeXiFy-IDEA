package nl.hannahsten.texifyidea.inspections.latex.typesetting

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexDisplayMath
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.parser.childrenOfType
import nl.hannahsten.texifyidea.util.parser.name

/**
 * @author Sten Wessel
 */
open class LatexQedHereInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override fun getDisplayName() = "Insert \\qedhere in trailing displaymath environment"

    override val inspectionId = "QedHere"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        // Only proof environments
        val displayMaths = file.childrenOfType(LatexEnvironment::class).asSequence()
            .filter { it.name()?.text == "proof" }
            // With no \qedhere command already present
            .filterNot { it.childrenOfType(LatexCommands::class).any { cmd -> cmd.name == "\\qedhere" } }
            // Ending in a displaymath environment
            .mapNotNull { it.environmentContent?.lastChild?.firstChild?.firstChild as? LatexDisplayMath }

        for (displayMath in displayMaths) {
            val mathContent = displayMath.mathContent ?: continue
            val offset = mathContent.startOffsetInParent + mathContent.textLength - 1

            descriptors.add(
                manager.createProblemDescriptor(
                    displayMath,
                    TextRange(offset, offset + 1),
                    "Missing \\qedhere in trailing displaymath environment",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly,
                    InsertQedHereFix()
                )
            )
        }

        return descriptors
    }

    /**
     * @author Sten Wessel
     */
    private class InsertQedHereFix : LocalQuickFix {

        override fun getFamilyName() = "Insert \\qedhere"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = (descriptor.psiElement as? LatexDisplayMath)?.mathContent ?: return
            val file = element.containingFile
            val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return

            document.insertString(element.textOffset + element.textLength, " \\qedhere")
        }
    }
}