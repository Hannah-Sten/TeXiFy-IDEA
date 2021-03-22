package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.childrenOfType

class LatexPackageNameDoesNotMatchFileNameInspection : TexifyInspectionBase() {

    override val inspectionGroup: InsightGroup = InsightGroup.LATEX

    override val inspectionId: String =
        "PackageNameDoesNotMatchFileName"

    override fun getDisplayName(): String {
        return "Package name does not match file name"
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val commands = file.childrenOfType(LatexCommands::class)
            .filter { it.name == "\\ProvidesPackage" }

        for (command in commands) {
            val providesName = command.requiredParameters.first().split("/").last()
            val fileName = file.name.removeSuffix(".sty")
            if (fileName != providesName) {
                descriptors.add(
                    manager.createProblemDescriptor(
                        command,
                        displayName,
                        PackageNameMatchFileNameQuickFix,
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                    )
                )
            }
        }

        return descriptors
    }

    object PackageNameMatchFileNameQuickFix : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Fix package name"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val providesCommand = descriptor.psiElement as LatexCommands
            val newCommandText = providesCommand.let {
                it.text.replace(
                    it.requiredParameters.first().split("/").last() + "}",
                    it.containingFile.name.removeSuffix(".sty") + "}"
                )
            }
            val newCommand = LatexPsiHelper(project).createFromText(newCommandText).firstChild

            val parent = providesCommand.parent
            parent.node.replaceChild(providesCommand.node, newCommand.node)
        }
    }
}