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
import nl.hannahsten.texifyidea.util.parser.childrenOfType
import nl.hannahsten.texifyidea.util.files.findRootFile
import java.io.File
import kotlin.math.max

class LatexPackageSubdirectoryInspection : TexifyInspectionBase() {

    override val inspectionGroup: InsightGroup = InsightGroup.LATEX

    override val inspectionId: String = "PackageSubdirectoryInspection"

    override fun getShortName(): String {
        return "LatexPackageSubdirectory"
    }

    override fun getDisplayName(): String =
        "Package name does not have the correct directory"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()
        val commands = file.childrenOfType(LatexCommands::class)
            .filter { it.name == "\\ProvidesPackage" }

        for (command in commands) {
            val parameter = command.getRequiredParameters().firstOrNull() ?: continue
            val lastSlashIndex = parameter.indexOfLast { it == '/' }
            val providedDir = parameter.removeRange(max(0, lastSlashIndex), parameter.length)
            val rootDir = file.findRootFile().containingDirectory ?: continue
            val dir = file.containingDirectory ?: continue
            val subDir = dir.toString().removePrefix(rootDir.toString()).removePrefix(File.separator).replace(File.separatorChar, '/')
            if (subDir != providedDir) {
                descriptors.add(
                    manager.createProblemDescriptor(
                        command,
                        displayName,
                        FixSubdirectoryQuickFix(providedDir, if (providedDir.isEmpty()) "$subDir/" else subDir),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                    )
                )
            }
        }
        return descriptors
    }

    inner class FixSubdirectoryQuickFix(private val oldDir: String, private val newDir: String) : LocalQuickFix {

        override fun getFamilyName(): String =
            "Change LaTeX command to match directory structure"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val providesCommand = descriptor.psiElement as LatexCommands
            val newCommandText = providesCommand.text.replace(
                "{$oldDir", "{$newDir"
            )
            val newCommand = LatexPsiHelper(project).createFromText(newCommandText).firstChild

            val parent = providesCommand.parent
            parent.node.replaceChild(providesCommand.node, newCommand.node)
        }
    }
}