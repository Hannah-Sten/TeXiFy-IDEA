package nl.hannahsten.texifyidea.inspections.latex.redundancy

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.inspections.InsightGroup.LATEX
import nl.hannahsten.texifyidea.inspections.AbstractTexifyCommandBasedInspection
import nl.hannahsten.texifyidea.inspections.createDescriptor
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.nameWithoutSlash
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.parser.findNextAdjacentWhiteSpace
import nl.hannahsten.texifyidea.util.parser.findPrevAdjacentWhiteSpace

class LatexRedundantParInspection : AbstractTexifyCommandBasedInspection(
    inspectionId = "RedundantPar",
    inspectionGroup = LATEX,
) {

    override fun inspectCommand(command: LatexCommands, contexts: LContextSet, defBundle: DefinitionBundle, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        if (command.nameWithoutSlash != "par") return

        val prev = command.findPrevAdjacentWhiteSpace()
        val next = command.findNextAdjacentWhiteSpace()
        if (lineBreakCount(prev) + lineBreakCount(next) >= 2) {
            // There are already blank lines around this \par
            descriptors.add(
                manager.createDescriptor(
                    command,
                    "Use of \\par is redundant here",
                    isOnTheFly = isOnTheFly,
                    fix = RemoveParQuickFix(),
                )
            )
        }
    }

    private fun lineBreakCount(element: PsiWhiteSpace?): Int {
        if (element == null) return 0
        return element.text.count { it == '\n' }
    }

    private class RemoveParQuickFix : LocalQuickFix {
        override fun getFamilyName() = "Remove \\par"
        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val range = descriptor.psiElement.textRange
            val document = descriptor.psiElement.containingFile.document() ?: return
            document.deleteString(range.startOffset, range.endOffset)
        }
    }
}