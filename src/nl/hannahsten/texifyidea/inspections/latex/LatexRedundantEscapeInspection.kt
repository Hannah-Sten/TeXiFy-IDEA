package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.Diacritic
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexContent
import nl.hannahsten.texifyidea.psi.LatexMathEnvironment
import nl.hannahsten.texifyidea.util.allCommands
import java.text.Normalizer
import java.util.*

/**
 * @author Sten Wessel
 */
open class LatexRedundantEscapeInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "RedundantEscape"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.COMMAND)!!

    override fun getDisplayName() = "Redundant escape when Unicode is enabled"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()
        if (!LatexUnicodeInspection.unicodeEnabled(file)) {
            return descriptors
        }

        val commands = file.allCommands()
        for (command in commands) {
            val inMathMode = PsiTreeUtil.getParentOfType(command, LatexMathEnvironment::class.java) != null
            if (inMathMode) {
                // Only works outside of math
                continue
            }

            val diacritic = Diacritic.Normal.fromCommand(command.commandToken.text) ?: continue
            if (diacritic.isTypeable && (command.requiredParameters.isNotEmpty() || hasNormalTextSibling(command))) {
                descriptors.add(
                    manager.createProblemDescriptor(
                        command,
                        "Redundant diacritic escape",
                        RemoveEscapeFix(),
                        ProblemHighlightType.WEAK_WARNING,
                        isOntheFly
                    )
                )
            }
        }

        return descriptors
    }

    private fun hasNormalTextSibling(command: LatexCommands): Boolean {
        val content = PsiTreeUtil.getParentOfType(command, LatexContent::class.java)
        val siblingContent = PsiTreeUtil.getNextSiblingOfType(content, LatexContent::class.java)
        return siblingContent?.noMathContent?.normalText != null
    }

    /**
     * @author Sten Wessel
     */
    private class RemoveEscapeFix : LocalQuickFix {

        override fun getFamilyName() = "Replace escape with Unicode character"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val file = command.containingFile
            val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return

            // Get diacritic information
            val diacritic = Diacritic.Normal.fromCommand(command.commandToken.text) ?: return

            val range: TextRange
            val base: String
            val param = command.requiredParameters.getOrNull(0)
            if (param != null) {
                // Just a required parameter
                range = command.textRange
                base = param
            }
            else {
                // Now find a sibling
                val content = PsiTreeUtil.getParentOfType(command, LatexContent::class.java)
                val siblingContent = PsiTreeUtil.getNextSiblingOfType(content, LatexContent::class.java)
                val siblingText = siblingContent?.noMathContent?.normalText

                if (siblingText != null) {
                    base = siblingText.text
                    range = command.textRange.union(siblingText.textRange)
                }
                else {
                    base = ""
                    range = TextRange.EMPTY_RANGE
                }
            }

            if (base.isEmpty()) {
                val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
                HintManager.getInstance().showErrorHint(editor, "Cannot insert replacement because the base character is missing")
                return
            }

            // Replace unicode char
            var replacement = base[0] + diacritic.unicode
            if (base.length > 1) {
                replacement += base.substring(1)
            }
            replacement = Normalizer.normalize(replacement, Normalizer.Form.NFC)

            document.replaceString(range.startOffset, range.endOffset, replacement)
        }
    }
}