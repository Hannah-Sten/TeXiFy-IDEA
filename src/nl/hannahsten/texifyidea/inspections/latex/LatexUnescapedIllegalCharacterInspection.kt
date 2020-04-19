package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexComment
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.hasParent
import nl.hannahsten.texifyidea.util.inMathContext

/**
 * @author Johannes Berger
 */
open class LatexUnescapedIllegalCharacterInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override fun getDisplayName() = "Special characters need to be escaped"

    override val inspectionId = "UnescapedIllegalCharacter"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val texts = file.childrenOfType(LatexNormalText::class)
        for (text: LatexNormalText in texts) {
            if (text.inMathContext() || text.hasParent(LatexComment::class)) {
                continue
            }

            val matcher = Magic.Pattern.unescapedSpecialCharacters.matcher(text.text)
            while (matcher.find()) {

                descriptors.add(manager.createProblemDescriptor(
                        text,
                        when (matcher.end() - matcher.start()) {
                            2 -> TextRange(matcher.start() + 1, matcher.end()) // regex captured non-escape char before special char
                            else -> TextRange(matcher.start(), matcher.end()) // beginning of the line
                        },
                        "Special characters need to be escaped",
                        ProblemHighlightType.WARNING,
                        isOntheFly,
                        InspectionFix()
                ))
            }
        }

        return descriptors
    }

    private class InspectionFix : LocalQuickFix {

        override fun getFamilyName() = "Insert escape character"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val textElement = descriptor.psiElement
            val document = textElement.containingFile.document() ?: return

            val offset = textElement.textOffset + descriptor.textRangeInElement.startOffset
            val escapeCharacter = "\\"
            document.insertString(offset, escapeCharacter)
        }
    }

}
