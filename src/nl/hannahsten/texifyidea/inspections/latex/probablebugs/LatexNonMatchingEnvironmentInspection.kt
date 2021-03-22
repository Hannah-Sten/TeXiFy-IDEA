package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.psi.LatexEndCommand
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.document
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class LatexNonMatchingEnvironmentInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "NonMatchingEnvironment"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Non matching environment commands"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val begins = file.childrenOfType(LatexBeginCommand::class)
        for (begin in begins) {
            val end = begin.endCommand() ?: continue
            val beginEnvironment = begin.environmentName() ?: continue
            val endEnvironment = end.environmentName() ?: continue
            if (beginEnvironment == endEnvironment) {
                continue
            }

            // Add descriptor to begin.
            descriptors.add(
                manager.createProblemDescriptor(
                    begin,
                    "DefaultEnvironment name does not match with the name in \\end.",
                    MatchBeginFix(beginEnvironment),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly
                )
            )

            // Add descriptor to end.
            descriptors.add(
                manager.createProblemDescriptor(
                    end,
                    "DefaultEnvironment name does not match with the name in \\begin.",
                    MatchEndFix(endEnvironment),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly
                )
            )
        }

        return descriptors
    }

    /**
     * @author Hannah Schellekens
     */
    private open class MatchBeginFix(val environmentName: String) : LocalQuickFix {

        override fun getFamilyName() = "Change \\end environment to '$environmentName'"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexBeginCommand
            val file = command.containingFile
            val document = file.document() ?: return
            val end = command.endCommand() ?: return
            val environment = command.environmentName()

            document.replaceString(end.textOffset, end.endOffset(), "\\end{$environment}")
        }
    }

    /**
     * @author Hannah Schellekens
     */
    private open class MatchEndFix(val environmentName: String) : LocalQuickFix {

        override fun getFamilyName() = "Change \\begin environment to '$environmentName'"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexEndCommand
            val file = command.containingFile
            val document = file.document() ?: return
            val begin = command.beginCommand() ?: return
            val environment = command.environmentName()

            document.replaceString(begin.textOffset, begin.endOffset(), "\\begin{$environment}")
        }
    }
}