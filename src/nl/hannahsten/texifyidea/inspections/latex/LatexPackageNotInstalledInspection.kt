package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.*

class LatexPackageNotInstalledInspection : TexifyInspectionBase() {
    override val inspectionGroup: InsightGroup = InsightGroup.LATEX

    override val inspectionId: String =
            "PackageNotInstalled"

    override fun getDisplayName(): String {
        return "Package is not installed"
    }

    override fun isEnabledByDefault(): Boolean {
        return LatexDistribution.isTexlive
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()
        if (LatexDistribution.isTexlive) {
            val installedPackages = TexLivePackages.packageList
            val customPackages = LatexDefinitionIndex.getCommandsByName("\\ProvidesPackage", file.project, file.project
                            .projectSearchScope)
                    .map { it.requiredParameter(0) }
                    .map { it?.toLowerCase() }
            val packages = installedPackages + customPackages

            val commands = file.childrenOfType(LatexCommands::class)
                    .filter { it.name == "\\usepackage" || it.name == "\\RequirePackage" }

            for (command in commands) {
                val `package` = command.requiredParameters.first().toLowerCase()
                if (`package` !in packages) {
                    descriptors.add(manager.createProblemDescriptor(
                            command,
                            "Package is not installed",
                            Magic.General.noQuickFix,
                            ProblemHighlightType.WARNING,
                            isOntheFly
                    ))
                }
            }
        }
        return descriptors
    }
}