package nl.hannahsten.texifyidea.inspections.latex.typesetting.spacing

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.inspections.AbstractTexifyCommandBasedInspection
import nl.hannahsten.texifyidea.inspections.createDescriptor
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.parser.collectSubtreeTyped
import nl.hannahsten.texifyidea.util.parser.lastChildOfType
import nl.hannahsten.texifyidea.util.parser.parentOfType
import java.util.*

/**
 * For now, only not using it before `\ref` or `\cite` will be detected.
 *
 * @author Hannah Schellekens
 */
class LatexNonBreakingSpaceInspection : AbstractTexifyCommandBasedInspection(
    inspectionId = "NonBreakingSpace",
    applicableContexts = setOf(LatexContexts.Text)
) {
    val inspectCommandNames = setOf(
        "\\ref",
        "\\cite"
    )

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.COMMAND)!!

    override fun inspectCommand(command: LatexCommands, contexts: LContextSet, defBundle: DefinitionBundle, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        val name = command.nameWithSlash ?: return
        if (name !in inspectCommandNames) return
        if (!isApplicableInContexts(contexts)) return
        // Get the NORMAL_TEXT in front of the command.
        val sibling = command.prevSibling
            ?: command.parent?.prevSibling
            ?: command.parentOfType(LatexNoMathContent::class)?.prevSibling
            ?: return
        if (sibling !is PsiWhiteSpace) return
        // When sibling is whitespace, it's obviously bad news. Must not have a newline
        val previousSentence = sibling.prevSibling
            ?: sibling.parent?.prevSibling
            ?: sibling.parentOfType(LatexNoMathContent::class)?.prevSibling
            ?: return
        val lastBitOfText = previousSentence.lastChildOfType(LatexNormalText::class) ?: previousSentence.lastChildOfType(LatexParameterText::class) ?: return
        if (!PatternMagic.sentenceSeparatorAtLineEnd.matcher(file.text.subSequence(lastBitOfText.startOffset, lastBitOfText.endOffset)).find()) {
            descriptors.add(
                manager.createDescriptor(
                    sibling,
                    "Reference without a non-breaking space",
                    isOnTheFly = isOnTheFly,
                    highlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    fix = WhitespaceReplacementFix(),
                )
            )
        }
    }

    /**
     * Replaces a complete [PsiWhiteSpace] element with `~`.
     *
     * @author Hannah Schellekens
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
            val texts = whitespace.prevSibling.collectSubtreeTyped<LatexNormalText>()
            if (!texts.isEmpty()) {
                val text = texts.reversed().iterator().next()

                // When there is a tilde, destroy the whitespace.
                if (PatternMagic.endsWithNonBreakingSpace.matcher(text.text).find()) {
                    replacement = ""
                }
            }

            // Otherwise, just replace all the whitespace by a tilde.
            document.replaceString(offset, offset + whitespace.textLength, replacement)
        }
    }
}
