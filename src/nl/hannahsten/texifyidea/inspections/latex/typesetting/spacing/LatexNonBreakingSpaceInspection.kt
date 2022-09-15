package nl.hannahsten.texifyidea.inspections.latex.typesetting.spacing

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand.*
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexNoMathContent
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.includedPackages
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.parentOfType
import java.util.*

/**
 * For now, only not using it before `\ref` or `\cite` will be detected.
 *
 * @author Hannah Schellekens
 */
open class LatexNonBreakingSpaceInspection : TexifyInspectionBase() {

    companion object {

        /**
         * All commands that should not have a forced breaking space.
         */
        val IGNORED_COMMANDS = setOf(
            "\\citet", "\\citet*", "\\Citet", "\\Citet*", "\\cref", "\\Cref", "\\cpageref", "\\autoref", "\\citeauthor", "\\textcite", "\\Textcite"
        )

        /**
         * Commands redefined by cleveref, such that no non-breaking space is needed anymore.
         */
        val CLEVEREF_REDEFINITIONS = setOf(THREF, VREF, VREFRANGE, FULLREF).map { it.commandWithSlash }
    }

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "NonBreakingSpace"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.COMMAND)!!

    override fun getDisplayName() = "Non-breaking spaces before references"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()
        val isCleverefLoaded = file.includedPackages().contains(LatexPackage.CLEVEREF)

        val commands = file.commandsInFile()
        for (command in commands) {
            // Only target references.
            if (!CommandMagic.reference.contains(command.name)) continue

            // Don't consider certain commands.
            if (command.name in IGNORED_COMMANDS) continue

            // Don't out-clever cleveref
            if (isCleverefLoaded && command.name in CLEVEREF_REDEFINITIONS) continue

            // Get the NORMAL_TEXT in front of the command.
            val sibling = command.prevSibling
                ?: command.parent?.prevSibling
                ?: command.parentOfType(LatexNoMathContent::class)?.prevSibling
                ?: continue

            // When sibling is whitespace, it's obviously bad news.
            if (sibling is PsiWhiteSpace) {
                descriptors.add(
                    manager.createProblemDescriptor(
                        sibling,
                        "Reference without a non-breaking space",
                        WhitespaceReplacementFix(),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                    )
                )
                continue
            }
        }

        return descriptors
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
            val texts = whitespace.prevSibling.childrenOfType(LatexNormalText::class)
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