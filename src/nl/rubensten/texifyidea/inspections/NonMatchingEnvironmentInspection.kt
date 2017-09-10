package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.psi.LatexBeginCommand
import nl.rubensten.texifyidea.psi.LatexEndCommand
import nl.rubensten.texifyidea.util.*
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * @author Ruben Schellekens
 */
open class NonMatchingEnvironmentInspection : TexifyInspectionBase() {

    override fun getDisplayName(): String {
        return "Non matching environment commands"
    }

    override fun getInspectionId(): String {
        return "NonMatchingEnvironment"
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        val begins = file.childrenOfType(LatexBeginCommand::class)
        for (begin in begins) {
            val end = begin.endCommand() ?: continue
            val beginEnvironment = begin.environmentName() ?: continue
            val endEnvironment = end.environmentName() ?: continue
            if (beginEnvironment == endEnvironment) {
                continue
            }

            // Add descriptor to begin.
            descriptors.add(manager.createProblemDescriptor(
                    begin,
                    "DefaultEnvironment name does not match with the name in \\end.",
                    MatchBeginFix(beginEnvironment),
                    ProblemHighlightType.GENERIC_ERROR,
                    isOntheFly
            ))

            // Add descriptor to end.
            descriptors.add(manager.createProblemDescriptor(
                    end,
                    "DefaultEnvironment name does not match with the name in \\begin.",
                    MatchEndFix(endEnvironment),
                    ProblemHighlightType.GENERIC_ERROR,
                    isOntheFly
            ))
        }

        return descriptors
    }

    /**
     * @author Ruben Schellekens
     */
    private open class MatchBeginFix(val environmentName: String) : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Change \\end environment to '$environmentName'"
        }

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
     * @author Ruben Schellekens
     */
    private open class MatchEndFix(val environmentName: String) : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Change \\begin environment to '$environmentName'"
        }

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