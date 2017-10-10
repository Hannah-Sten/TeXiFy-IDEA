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
import nl.rubensten.texifyidea.psi.LatexNormalText
import nl.rubensten.texifyidea.util.childrenOfType
import nl.rubensten.texifyidea.util.document
import nl.rubensten.texifyidea.util.firstChildOfType
import org.intellij.lang.annotations.Language
import java.util.regex.Pattern
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * For now, only not using it before `\ref` or `\cite` will be detected.
 *
 * @author Ruben Schellekens
 */
open class LatexNonBreakingSpaceInspection : TexifyInspectionBase() {

    companion object {

        /**
         * All the commands that require a non-breaking space in front.
         */
        @JvmStatic val REFERENCE_COMMANDS = setOf(
                "\\ref", "\\cite", "\\eqref", "\\nameref", "\\autoref",
                "\\fullref", "\\pageref"
        )

        /**
         * Matches when a string doesn't end with a non-breaking space (`~`).
         */
        @Language("RegExp")
        private val ENDS_WITH_NON_BREAKING_SPACE = Pattern.compile("~$")
    }

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getDisplayName() = "Start sentences on a new line"

    override fun getInspectionId() = "NonBreakingSpace"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        val commands = LatexCommandsIndex.getIndexedCommands(file)
        for (cmd in commands) {
            if (!REFERENCE_COMMANDS.contains(cmd.name)) {
                continue
            }

            // Get the NORMAL_TEXT in front of the command.
            val sibling = cmd.parent.parent.prevSibling ?: continue

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

            // Otherwise, it's CONTENT - so check the underlying normal text if it ends with whitespace.
            val text = sibling.firstChildOfType(LatexNormalText::class)?.text ?: continue
            val matcher = ENDS_WITH_NON_BREAKING_SPACE.matcher(text)
            if (!matcher.find()) {
                descriptors.add(manager.createProblemDescriptor(
                        cmd,
                        "Reference without a non-breaking space",
                        TextReplacementFix(),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                ))
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

        override fun getFamilyName(): String {
            return "Insert non-breaking space"
        }

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
                if (ENDS_WITH_NON_BREAKING_SPACE.matcher(text.text).find()) {
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

        override fun getFamilyName(): String {
            return "Insert non-breaking space"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val file = command.containingFile
            val document = file.document() ?: return

            document.insertString(command.textOffset, "~")
        }
    }
}