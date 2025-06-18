package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.util.parser.childrenOfType
import nl.hannahsten.texifyidea.util.parser.traverse
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
        var documentClass : LatexCommands? = null
        // file - content - no_math_content - commands
        file.traverse(depth = 3) {
            if(it is LatexCommands && it.name == "\\documentclass") {
                documentClass = it
                false // Stop traversing once we found the document class
            }else{
                true
            }
        }
        if(documentClass == null) return emptyList()

        val hasDocumentEnvironment = file.traverse(depth = 3) {
            !(it is LatexEnvironment && it.getEnvironmentName() == "document") // Stop traversing once we found the document environment
        }

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