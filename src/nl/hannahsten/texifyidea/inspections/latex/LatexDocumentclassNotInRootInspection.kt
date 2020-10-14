package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.firstChildOfType
import org.jetbrains.annotations.Nls

class LatexDocumentclassNotInRootInspection : TexifyInspectionBase() {
    override val inspectionGroup: InsightGroup
        get() = InsightGroup.LATEX

    @Nls
    override fun getDisplayName(): String {
        return "File that contains a document environment should contain a \\documentclass command"
    }

    override val inspectionId: String
        get() = "DocumentclassNotInRoot"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val documentClass = file.commandsInFile().find { it.name == "\\documentclass" } ?: return emptyList()

        val hasDocumentEnvironment = file.firstChildOfType(LatexEnvironment::class)?.environmentName == "document"

        if (!hasDocumentEnvironment) {
            return listOf(
                    manager.createProblemDescriptor(
                            documentClass,
                            displayName,
                            true,
                            ProblemHighlightType.WARNING,
                            isOntheFly
                    )
            )
        }
        return emptyList()
    }
}