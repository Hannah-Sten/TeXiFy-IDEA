package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.psi.LatexNormalText
import nl.rubensten.texifyidea.util.childrenOfType
import nl.rubensten.texifyidea.util.document
import nl.rubensten.texifyidea.util.inMathMode
import nl.rubensten.texifyidea.util.lineIndentation
import org.intellij.lang.annotations.Language
import java.util.regex.Pattern
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * @author Ruben Schellekens
 */
open class LineBreakInspection : TexifyInspectionBase() {

    companion object {
        /**
         * Matches the end of a sentence.
         *
         * Includes `[^.][^.]` because of abbreviations (at least in Dutch) like `s.v.p.`
         */
        @Language("RegExp")
        private val SENTENCE_END = Pattern.compile("([^.][^.][.?!;;] +)|(^\\. )")

        @Language("RegExp")
        private val SENTENCE_SEPERATOR = Pattern.compile("[.?!;;]")
    }

    override fun getDisplayName(): String {
        return "Start sentences on a new line"
    }

    override fun getShortName(): String {
        return "LineBreak"
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()
        val document = file.document() ?: return descriptors

        // Psi version
        val texts = file.childrenOfType(LatexNormalText::class)
        for (text: LatexNormalText in texts) {
            if (text.inMathMode()) {
                continue
            }

            val matcher = SENTENCE_END.matcher(text.text)
            while (matcher.find()) {
                val offset = text.textOffset + matcher.end()
                val lineNumber = document.getLineNumber(offset)
                val endLine = document.getLineEndOffset(lineNumber)

                descriptors.add(manager.createProblemDescriptor(
                        text,
                        TextRange(matcher.start(), matcher.end() + (endLine - offset)),
                        "Sentence does not start on a new line",
                        ProblemHighlightType.WEAK_WARNING,
                        isOntheFly,
                        InspectionFix()
                ))
            }
        }

        return descriptors
    }

    private class InspectionFix : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Insert line feed"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val textElement = descriptor.psiElement
            val document = textElement.containingFile.document() ?: return
            val matcher = SENTENCE_END.matcher(textElement.text)
            val range = descriptor.textRangeInElement
            val textOffset = textElement.textOffset
            val textInElement = document.getText(TextRange(textOffset + range.startOffset, textOffset + range.endOffset))

            if (!matcher.find()) {
                return
            }

            val signMarker = SENTENCE_SEPERATOR.matcher(textInElement)
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