package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.ClassFileType
import nl.hannahsten.texifyidea.file.StyleFileType
import nl.hannahsten.texifyidea.inspections.AbstractTexifyCommandBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.nameWithoutSlash

class LatexUsePackageInPackageInspection : AbstractTexifyCommandBasedInspection(
    inspectionId = "UsePackageInPackage"
) {
    private val applicableFileExtensions = setOf(ClassFileType, StyleFileType)

    override fun isAvailableForFile(file: PsiFile): Boolean {
        val fileType = file.virtualFile?.fileType ?: file.fileType
        // don't know why but sometimes file.fileType is not the same as file.virtualFile?.fileType
        return fileType in applicableFileExtensions
    }

    override fun inspectCommand(command: LatexCommands, contexts: LContextSet, lookup: LatexSemanticsLookup, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        val name = command.nameWithoutSlash ?: return
        if (name != "usepackage") return
        val descriptor = manager.createProblemDescriptor(
            command,
            "Use \\RequirePackage{...} instead of \\usepackage{...}",
            ReplaceCommandQuickFix("Replace with \\RequirePackage", "RequirePackage"),
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            isOnTheFly
        )
        descriptors.add(descriptor)
    }
}
