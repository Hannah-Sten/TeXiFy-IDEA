package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.files.findRootFile
import kotlin.math.max

class LatexPackageSubdirectoryInspection : TexifyInspectionBase() {
    override val inspectionGroup: InsightGroup = InsightGroup.LATEX

    override val inspectionId: String = "PackageSubdirectoryInspection"

    override fun getShortName(): String {
        return "LatexPackageSubdirectoryInspection"
    }

    override fun getDisplayName(): String =
            "Package name does not have the correct directory"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()
        val commands = file.childrenOfType(LatexCommands::class)
                .filter { it.name == "\\ProvidesPackage"  }

        for (command in commands) {
            val parameter = command.requiredParameters.first()
            val lastSlashIndex = parameter.indexOfLast { it == '/' }
            val providedDir = parameter.removeRange(max(0, lastSlashIndex), parameter.length)
            val rootDir = file.findRootFile().containingDirectory
            val dir = file.containingDirectory
            val subDir = dir.toString().removePrefix(rootDir.toString()).removePrefix("/")
            if (subDir != providedDir) {
                descriptors.add(manager.createProblemDescriptor(
                        command,
                        displayName,
                        FixSubdirectoryQuickFix,
                        ProblemHighlightType.WARNING,
                        isOntheFly
                ))
            }
        }
        return descriptors
    }

    object FixSubdirectoryQuickFix : LocalQuickFix {
        override fun getFamilyName(): String =
                "Package name does not have the correct directory"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            // Do nothing.
        }

    }
}