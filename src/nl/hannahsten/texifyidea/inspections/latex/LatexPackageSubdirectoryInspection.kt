package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase

class LatexPackageSubdirectoryInspection : TexifyInspectionBase() {
    override val inspectionGroup: InsightGroup = InsightGroup.LATEX

    override val inspectionId: String = "PackageSubdirectoryInspection"

    override fun getDisplayName(): String =
            "Package name does not have the correct directory"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        return descriptors
    }
}