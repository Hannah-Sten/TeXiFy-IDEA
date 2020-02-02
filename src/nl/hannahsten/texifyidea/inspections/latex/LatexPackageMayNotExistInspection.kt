package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.PackageUtils
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.projectSearchScope

class LatexPackageMayNotExistInspection : TexifyInspectionBase() {
    override val inspectionGroup: InsightGroup = InsightGroup.LATEX

    override val inspectionId: String =
            "PackageMayNotExist"

    override fun getDisplayName(): String {
        return "Package may not exist"
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()
        val ctanPackages = PackageUtils.CTAN_PACKAGE_NAMES
        val customPackages = LatexDefinitionIndex.getCommandsByName("\\ProvidesPackage", file.project, file.project.projectSearchScope)
        val packages = ctanPackages + customPackages

        val commands = file.childrenOfType(LatexCommands::class)
                .filter { it.name == "\\usepackage" || it.name == "\\RequirePackage" }

        for (command in commands) {
            val `package` = command.requiredParameters.first()
            if (!packages.contains(`package`)) {
                descriptors.add(manager.createProblemDescriptor(
                        command,
                        "Package may not exist",
                        Magic.General.noQuickFix,
                        ProblemHighlightType.WARNING,
                        isOntheFly
                ))
            }
        }

        return descriptors
    }
}