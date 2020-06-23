package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexMathContent
import nl.hannahsten.texifyidea.psi.LatexPsiUtil.getNextSiblingIgnoreWhitespace
import nl.hannahsten.texifyidea.psi.LatexPsiUtil.getPreviousSiblingIgnoreWhitespace
import nl.hannahsten.texifyidea.util.deleteElement
import org.jetbrains.annotations.Nls
import java.util.EnumSet

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
        val commands = LatexCommandsIndex.getItems(file)
        for (command in commands) {
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
            var content = element.parent.parent
            if (content is LatexMathContent) {
                content = element.parent
            }
            val previous = getPreviousSiblingIgnoreWhitespace(content!!)
            val next = getNextSiblingIgnoreWhitespace(content)
            val before = if (previous == null) "" else previous.text
            val after = if (next == null) "" else next.text
            val replacement = String.format("\\frac{%s}{%s}", before, after)
            val document =
                PsiDocumentManager.getInstance(project).getDocument(element.containingFile)

            // Delete denominator.
            if (next != null) {
                document!!.deleteElement(next)
            }

            // Replace command.
            val range = element.commandToken.textRange
            document?.replaceString(range.startOffset, range.endOffset, replacement)

            // Replace numerator.
            if (previous != null) {
                document!!.deleteElement(previous)
            }
        }
    }
}