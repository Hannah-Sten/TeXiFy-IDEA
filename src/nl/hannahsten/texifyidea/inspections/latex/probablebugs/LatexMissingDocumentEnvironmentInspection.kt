package nl.hannahsten.texifyidea.inspections.latex.probablebugs

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
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.psi.environmentName
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.parser.traverseTyped
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class LatexMissingDocumentEnvironmentInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "MissingDocumentEnvironment"

    override val ignoredSuppressionScopes = EnumSet.of(
        MagicCommentScope.ENVIRONMENT,
        MagicCommentScope.MATH_ENVIRONMENT,
        MagicCommentScope.COMMAND,
        MagicCommentScope.GROUP
    )!!

    override fun getDisplayName() = "Missing document environment"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        // Workaround, because .sty files also return LatexFileType.INSTANCE
        if (file.virtualFile.extension != LatexFileType.defaultExtension) {
            return descriptors
        }

        for (referencedFile in file.referencedFileSet()) {
            val hasBeginCommand = referencedFile.traverseTyped<LatexBeginCommand>()
                .any { it.environmentName() == DefaultEnvironment.DOCUMENT.environmentName }
            if (hasBeginCommand) {
                return descriptors
            }
        }

        descriptors.add(
            manager.createProblemDescriptor(
                file,
                "Document doesn't contain a document environment.",
                InspectionFix(),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOntheFly
            )
        )

        return descriptors
    }

    /**
     * @author Hannah Schellekens
     */
    private class InspectionFix : LocalQuickFix {

        override fun getFamilyName() = "Add a document environment"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val psiElement = descriptor.psiElement
            val file = psiElement.containingFile
            val document = PsiDocumentManager.getInstance(project).getDocument(file)

            document?.insertString(
                document.textLength,
                """
                |
                |\begin{document}
                |
                |\end{document}
                """.trimMargin()
            )
        }
    }
}