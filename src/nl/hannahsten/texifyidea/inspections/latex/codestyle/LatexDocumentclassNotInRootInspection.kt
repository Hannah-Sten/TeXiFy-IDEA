package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.files.commandsInFile
import org.jetbrains.annotations.Nls

class LatexDocumentclassNotInRootInspection : TexifyInspectionBase() {

    override val inspectionGroup: InsightGroup
        get() = InsightGroup.LATEX

    @Nls
    override fun getDisplayName(): String {
        return "Documentclass command should be in the same file as the document environment"
    }

    override val inspectionId: String
        get() = "DocumentclassNotInRoot"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val documentClass = file.commandsInFile().find { it.name == "\\documentclass" } ?: return emptyList()

        val hasDocumentEnvironment = file.childrenOfType<LatexEnvironment>().any { it.environmentName == "document" }

        if (!hasDocumentEnvironment) {
            return listOf(
                manager.createProblemDescriptor(
                    documentClass,
                    displayName,
                    true,
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly
                )
            )
        }
        return emptyList()
    }
}