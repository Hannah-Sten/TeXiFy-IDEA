package nl.hannahsten.texifyidea.inspections.latex.codematurity

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.toTextRange
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Hannah Schellekens
 */
open class LatexPrimitiveEquationInspection : TexifyRegexInspection(
    inspectionDisplayName = "Discouraged use of primitive TeX display math",
    inspectionId = "PrimitiveEquation",
    errorMessage = { "Use '\\[..\\]' instead of primitive TeX display math." },
    pattern = Pattern.compile("(\\\$\\\$)[^\$]*\\\$?[^\$]*(\\\$\\\$)"),
    mathMode = false,
    replacement = { _, _ -> "" },
    replacementRange = Util::replaceRange,
    highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    highlightRange = { Util.replaceRange(it).toTextRange().grown(-1) },
    quickFixName = { "Replace with '\\[..\\]'" }
) {

    object Util {
        fun replaceRange(it: Matcher) = (it.groupRange(1).first..it.groupRange(2).last)
    }

    override fun applyFix(descriptor: ProblemDescriptor, replacementRange: IntRange, replacement: String, groups: List<String>): Int {
        val file = descriptor.psiElement as PsiFile
        val document = file.document() ?: return 0

        document.replaceString(replacementRange.first, replacementRange.first + 2, "\\[")
        document.replaceString(replacementRange.last - 2, replacementRange.last, "\\]")

        // $$ were replaced by \[ or \] so the length did not change
        return 0
    }
}