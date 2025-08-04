package nl.hannahsten.texifyidea.inspections.latex.codematurity

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNoMathContent
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.psi.forEachCommand
import nl.hannahsten.texifyidea.util.parser.nextSiblingIgnoreWhitespace
import nl.hannahsten.texifyidea.util.parser.parentOfType
import nl.hannahsten.texifyidea.util.parser.previousSiblingIgnoreWhitespace
import org.jetbrains.annotations.Nls
import java.util.*

/**
 * @author Hannah Schellekens
 */
class LatexOverInsteadOfFracInspection : TexifyInspectionBase() {

    override val inspectionGroup: InsightGroup
        get() = InsightGroup.LATEX

    @Nls
    override fun getDisplayName(): String {
        return "Discouraged use of \\over"
    }

    override val inspectionId: String
        get() = "OverInsteadOfFrac"

    override val outerSuppressionScopes: Set<MagicCommentScope>
        get() = EnumSet.of(MagicCommentScope.COMMAND)

    override fun inspectFile(
        file: PsiFile,
        manager: InspectionManager,
        isOntheFly: Boolean
    ): List<ProblemDescriptor> {
        val descriptors = descriptorList()
        file.forEachCommand { command ->
            if ("\\over" == command.name) {
                descriptors.add(
                    manager.createProblemDescriptor(
                        command,
                        "Use of \\over is discouraged",
                        OverToFracFix(),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                    )
                )
            }
        }
        return descriptors
    }

    private inner class OverToFracFix : LocalQuickFix {

        @Nls
        override fun getFamilyName(): String {
            return "Convert to \\frac"
        }

        override fun applyFix(
            project: Project,
            descriptor: ProblemDescriptor
        ) {
            val element = descriptor.psiElement as? LatexCommands ?: return

            // Find elements to put in numerator and denominator.
            val content = element.parentOfType(LatexNoMathContent::class)
            val previous = content!!.previousSiblingIgnoreWhitespace()
            val next = content.nextSiblingIgnoreWhitespace()
            val before = previous?.text ?: ""
            val after = next?.text ?: ""
            val psiReplacement = LatexPsiHelper(project).createFromText("\\frac{$before}{$after}").firstChild

            // Add the replacement in the psi tree.
            val bla = next ?: content
            bla.parent?.addAfter(psiReplacement, bla)
            // Remove the old fraction (numerator\over denominator), including a possible space between \over and the
            // denominator. Remove this space before removing the \over command so content still exists when removing
            // its next sibling. If there is no space after \over, this will remove the denominator (next) and removing
            // next does nothing.
            if (next != null) {
                content.nextSibling.delete()
                next.delete()
            }
            previous?.delete()
            content.delete()
        }
    }
}