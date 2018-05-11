package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.psi.LatexComment
import nl.rubensten.texifyidea.psi.LatexNormalText
import nl.rubensten.texifyidea.util.*
import kotlin.math.min

/**
 * @author Ruben Schellekens
 */
open class LatexLineBreakInspection : TexifyInspectionBase() {

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getDisplayName() = "Start sentences on a new line"

    override fun getInspectionId() = "LineBreak"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()
        val document = file.document() ?: return descriptors

        val texts = file.childrenOfType(LatexNormalText::class)
        for (text: LatexNormalText in texts) {
            if (text.inMathContext() || text.hasParent(LatexComment::class)) {
                continue
            }

            val matcher = Magic.Pattern.sentenceEnd.matcher(text.text)
            while (matcher.find()) {
                val offset = text.textOffset + matcher.end()
                val lineNumber = document.getLineNumber(offset)
                val endLine = document.getLineEndOffset(lineNumber)

                descriptors.add(manager.createProblemDescriptor(
                        text,
                        TextRange(matcher.start(), min(text.textLength, matcher.end() + (endLine - offset))),
                        "Sentence does not start on a new line",
                        ProblemHighlightType.WEAK_WARNING,
                        isOntheFly,
                        InspectionFix()
                ))
            }
        }

        return descriptors
    }

    /**
     * @author Ruben Schellekens
     */
    private class InspectionFix : LocalQuickFix {

        override fun getFamilyName() = "Insert line feed"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val textElement = descriptor.psiElement
            val document = textElement.containingFile.document() ?: return
            val matcher = Magic.Pattern.sentenceEnd.matcher(textElement.text)
            val range = descriptor.textRangeInElement
            val textOffset = textElement.textOffset
            val textInElement = document.getText(TextRange(textOffset + range.startOffset, textOffset + range.endOffset))

            if (!matcher.find()) {
                return
            }

            val signMarker = Magic.Pattern.sentenceSeperator.matcher(textInElement)
            if (!signMarker.find()) {
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
