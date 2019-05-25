package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.inspections.TexifyRegexInspection
import nl.rubensten.texifyidea.util.document
import nl.rubensten.texifyidea.util.toTextRange
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class LatexPrimitiveEquationInspection : TexifyRegexInspection(
        inspectionDisplayName = "Discouraged use of primitive TeX display math",
        myInspectionId = "PrimitiveEquation",
        errorMessage = { "Use '\\[..\\]' instead of primitive TeX display math." },
        pattern = Pattern.compile("(\\\$\\\$)[^\$]*\\\$?[^\$]*(\\\$\\\$)"),
        mathMode = false,
        replacement = { _, _ -> "" },
        replacementRange = this::replaceRange,
        highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
        highlightRange = { replaceRange(it).toTextRange() },
        quickFixName = { "Replace with '\\[..\\]'" }
) {

    companion object {

        fun replaceRange(it: Matcher) = (it.groupRange(1).start..it.groupRange(2).endInclusive)
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor, replacementRange: IntRange, replacement: String, groups: List<String>): Int {
        val file = descriptor.psiElement as PsiFile
        val document = file.document() ?: return 0

        document.replaceString(replacementRange.start, replacementRange.start + 2, "\\[")
        document.replaceString(replacementRange.endInclusive - 2, replacementRange.endInclusive, "\\]")

        // $$ were replaced by \[ or \] so the length did not change
        return 0
    }
}