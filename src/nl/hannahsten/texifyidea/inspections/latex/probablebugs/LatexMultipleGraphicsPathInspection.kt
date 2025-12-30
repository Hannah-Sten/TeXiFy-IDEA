package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.util.files.commandsInFile

/**
 * @author Lukas Heiligenbrunner
 */
class LatexMultipleGraphicsPathInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "MultipleGraphicsPath"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val commands = file.commandsInFile()

        // Check if a graphicspath is defined
        val paths = commands.filter { it.name == "\\graphicspath" }

        // Throw error on multiple definition of \graphicspath.
        if (paths.size > 1) {
            for (i in paths) {
                descriptors.add(
                    manager.createProblemDescriptor(
                        i,
                        TextRange(0, i.text.length),
                        "\\graphicspath is already used elsewhere",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly,
                        RemoveFix()
                    )
                )
            }
        }

        return descriptors
    }

    /**
     * Remove the command line.
     */
    class RemoveFix : LocalQuickFix {

        override fun getFamilyName(): String = "Remove this Line"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            descriptor.psiElement.delete()
        }
    }
}