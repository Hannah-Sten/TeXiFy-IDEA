package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.util.SmartList
import nl.rubensten.texifyidea.inspections.InspectionGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexDisplayMath
import nl.rubensten.texifyidea.psi.LatexEnvironment
import nl.rubensten.texifyidea.util.childrenOfType
import nl.rubensten.texifyidea.util.name

/**
 *
 * @author Sten Wessel
 */
open class LatexQedHereInspection : TexifyInspectionBase() {

    override fun getInspectionGroup() = InspectionGroup.LATEX

    override fun getDisplayName() = "Insert \\qedhere in trailing displaymath environment"

    override fun getInspectionId() = "QedHere"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        // Only proof environments
        val displayMaths = file.childrenOfType(LatexEnvironment::class).filter { it.name()?.text == "proof" }
                // With no \qedhere command already present
                .filterNot { it.childrenOfType(LatexCommands::class).any { it.name == "\\qedhere" } }
                // Ending in a displaymath environment
                .mapNotNull { it.environmentContent?.lastChild?.firstChild?.firstChild as? LatexDisplayMath }

        for (displayMath in displayMaths) {
            val mathContent = displayMath.mathContent ?: continue
            val offset = mathContent.startOffsetInParent + mathContent.textLength - 1

            descriptors.add(manager.createProblemDescriptor(
                    displayMath,
                    TextRange(offset, offset + 1),
                    "Missing \\qedhere in trailing displaymath environment",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly,
                    InsertQedHereFix()
            ))
        }

        return descriptors
    }


    private class InsertQedHereFix() : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Insert \\qedhere"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = (descriptor.psiElement as? LatexDisplayMath)?.mathContent ?: return
            val file = element.containingFile
            val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return

            document.insertString(element.textOffset + element.textLength, " \\qedhere")
        }

    }
}
