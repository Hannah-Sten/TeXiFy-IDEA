package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class LatexMissingDocumentclassInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "MissingDocumentclass"

    override val ignoredSuppressionScopes = EnumSet.of(
        MagicCommentScope.ENVIRONMENT,
        MagicCommentScope.MATH_ENVIRONMENT,
        MagicCommentScope.COMMAND,
        MagicCommentScope.GROUP
    )!!

    override fun getDisplayName() = "Missing documentclass"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        // Workaround, because .sty files also return LatexFileType.INSTANCE
        if (file.virtualFile.extension != LatexFileType.defaultExtension) {
            return descriptors
        }

        val hasDocumentclass = file.commandsInFileSet().asSequence()
            .filter { cmd -> cmd.name == "\\documentclass" }
            .count() > 0

        if (!hasDocumentclass) {
            descriptors.add(
                manager.createProblemDescriptor(
                    file,
                    "Document doesn't contain a \\documentclass command.",
                    InspectionFix(),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly
                )
            )
        }

        return descriptors
    }

    private class InspectionFix : LocalQuickFix {

        override fun getFamilyName() = "Add \\documentclass{article}"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val psiElement = descriptor.psiElement
            val file = psiElement.containingFile
            val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return

            document.insertString(0, "\\documentclass{article}\n")
        }
    }
}