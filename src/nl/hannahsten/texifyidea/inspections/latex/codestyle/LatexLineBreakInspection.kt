package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.startOffset
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.inspections.latex.typesetting.spacing.LatexSpaceAfterAbbreviationInspection
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.magic.PatternMagic.sentenceEndPrefix
import nl.hannahsten.texifyidea.util.psi.childrenOfType
import nl.hannahsten.texifyidea.util.psi.inMathContext
import nl.hannahsten.texifyidea.util.psi.isComment
import nl.hannahsten.texifyidea.util.psi.nextSiblingIgnoreWhitespace
import kotlin.math.min

/**
 * @author Hannah Schellekens
 */
open class LatexLineBreakInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override fun getDisplayName() = "Start sentences on a new line"

    override val inspectionId = "LineBreak"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()
        val document = file.document() ?: return descriptors

        // Target all regular text for this inspection.
        val texts = file.childrenOfType(LatexNormalText::class)
        for (text in texts) {
            // Do not trigger the inspection in math mode.
            if (text.inMathContext()) {
                continue
            }

            val matcher = PatternMagic.sentenceEnd.matcher(text.text)
            while (matcher.find()) {
                val offset = text.textOffset + matcher.end()
                val lineNumber = document.getLineNumber(offset)
                val endLine = document.getLineEndOffset(lineNumber)

                val startOffset = matcher.start()
                val endOffset = matcher.end() + (endLine - offset)

                val element = file.findElementAt(startOffset + text.startOffset)
                // Do not trigger the inspection when in a comment or when a comment starts directly after.
                if ((element?.isComment() == true) || (element?.nextSiblingIgnoreWhitespace()?.isComment() == true)) {
                    continue
                }

                // It may be that this inspection is incorrectly triggered on an abbreviation.
                // However, that means that the correct user action is to write a normal space after the abbreviation,
                // which is what we suggest with this quickfix.
                val dotPlusSpace = "^$sentenceEndPrefix(\\.\\s)".toRegex().find(text.text.substring(startOffset, matcher.end()))?.groups?.get(0)?.range?.shiftRight(startOffset + 1)
                val normalSpaceFix = if (dotPlusSpace != null) LatexSpaceAfterAbbreviationInspection.NormalSpaceFix(dotPlusSpace) else null
                val fixes = listOfNotNull(InspectionFix(), normalSpaceFix).toTypedArray()

                descriptors.add(
                    manager.createProblemDescriptor(
                        text,
                        TextRange(startOffset, min(text.textLength, endOffset)),
                        "Sentence does not start on a new line",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly,
                        *fixes
                    )
                )
            }
        }

        return descriptors
    }

    /**
     * @author Hannah Schellekens
     */
    private class InspectionFix : LocalQuickFix {

        override fun getFamilyName() = "Insert line feed"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val textElement = descriptor.psiElement
            val document = textElement.containingFile.document() ?: return
            val range = descriptor.textRangeInElement
            val textOffset = textElement.textOffset
            val textInElement = document.getText(TextRange(textOffset + range.startOffset, textOffset + range.endOffset))

            // Do not apply the fix when there is no break point.
            val signMarker = PatternMagic.sentenceSeparator.matcher(textInElement)
            if (signMarker.find().not()) {
                return
            }

            // Fill in replacement
            val offset = textElement.textOffset + descriptor.textRangeInElement.startOffset + signMarker.end() + 1
            val lineNumber = document.getLineNumber(offset)
            val replacement = "\n${document.lineIndentation(lineNumber)}"
            document.insertString(offset, replacement)
            document.deleteString(offset - 1, offset)
        }
    }
}
