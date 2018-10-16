package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexContent
import nl.rubensten.texifyidea.psi.LatexNormalText
import nl.rubensten.texifyidea.util.Magic
import nl.rubensten.texifyidea.util.childrenOfType
import nl.rubensten.texifyidea.util.document
import nl.rubensten.texifyidea.util.parentOfType

/**
 * For now, only not using it before `\ref` or `\cite` will be detected.
 *
 * @author Ruben Schellekens
 */
open class LatexNonBreakingSpaceInspection : TexifyInspectionBase() {

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getDisplayName() = "Start sentences on a new line"

    override fun getInspectionId() = "NonBreakingSpace"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val commands = LatexCommandsIndex.getItems(file)
        for (command in commands) {
            if (!Magic.Command.reference.contains(command.name)) {
                continue
            }

            // Get the NORMAL_TEXT in front of the command.
            val sibling = command.parentOfType(LatexContent::class)?.prevSibling ?: continue

            // When sibling is whitespace, it's obviously bad news.
            if (sibling is PsiWhiteSpace) {
                descriptors.add(manager.createProblemDescriptor(
                        sibling,
                        "Reference without a non-breaking space",
                        WhitespaceReplacementFix(),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                ))
                continue
            }
        }

        return descriptors
    }

    /**
     * Replaces a complete [PsiWhiteSpace] element with `~`.
     *
     * @author Ruben Schellekens
     */
    private class WhitespaceReplacementFix : LocalQuickFix {

        override fun getFamilyName() = "Insert non-breaking space"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val whitespace = descriptor.psiElement as PsiWhiteSpace
            val file = whitespace.containingFile
            val document = file.document() ?: return
            val offset = whitespace.textOffset
            var replacement = "~"

            // First check if there already is a tilde in the normal text before.
            val texts = whitespace.prevSibling.childrenOfType(LatexNormalText::class)
            if (!texts.isEmpty()) {
                val text = texts.reversed().iterator().next()

                // When there is a tilde, destroy the whitespace.
                if (Magic.Pattern.endsWithNonBreakingSpace.matcher(text.text).find()) {
                    replacement = ""
                }
            }

            // Otherwise, just replace all the whitespace by a tilde.
            document.replaceString(offset, offset + whitespace.textLength, replacement)
        }
    }

    /**
     * Replaces the ending of [LatexNormalText] element with `~`.
     *
     * @author Ruben Schellekens
     */
    private class TextReplacementFix : LocalQuickFix {

        override fun getFamilyName() = "Insert non-breaking space"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val file = command.containingFile
            val document = file.document() ?: return

            document.insertString(command.textOffset, "~")
        }
    }
}