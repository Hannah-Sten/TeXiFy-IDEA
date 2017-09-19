package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.util.SmartList
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.lang.Package
import nl.rubensten.texifyidea.psi.LatexNormalText
import nl.rubensten.texifyidea.util.*

/**
 * @author Sten Wessel
 */
open class LatexEllipsisInspection : TexifyInspectionBase() {

    companion object {
        private val ELLIPSIS = Regex("""(?<!\.)(\.\.\.)(?!\.)""")
    }

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getDisplayName() = "Ellipsis with ... instead of \\ldots or \\dots"

    override fun getInspectionId() = "Ellipsis"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()
        val texts = file.childrenOfType(LatexNormalText::class)

        for (text in texts) {
            ProgressManager.checkCanceled()

            for (match in ELLIPSIS.findAll(text.text)) {
                descriptors.add(manager.createProblemDescriptor(
                        text,
                        match.range.toTextRange(),
                        "Ellipsis with ... instead of command",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly,
                        InsertEllipsisCommandFix(text.inMathContext())
                ))
            }
        }

        return descriptors
    }


    private class InsertEllipsisCommandFix(val inMathMode: Boolean) : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Convert to ${if (inMathMode) "\\dots (amsmath package)" else "\\ldots"}"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement
            val file = element.containingFile
            val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return
            val range = descriptor.textRangeInElement.shiftRight(element.textOffset)

            document.replaceString(range.startOffset, range.endOffset, if (inMathMode) "\\dots" else "\\ldots")

            if (inMathMode && Package.AMSMATH.name !in file.includedPackages()) {
                file.insertUsepackage(Package.AMSMATH)
            }
        }

    }
}
