package nl.hannahsten.texifyidea.inspections.latex.typesetting

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import nl.hannahsten.texifyidea.inspections.TexifyContextAwareRegexInspectionBase
import nl.hannahsten.texifyidea.lang.LatexContexts

// /**
// * @author Hannah Schellekens
// */
// open class LatexCdotInspection : TexifyRegexInspection(
//    inspectionDisplayName = "Use of . instead of \\cdot",
//    inspectionId = "Cdot",
//    errorMessage = { "\\cdot expected" },
//    pattern = Pattern.compile("\\s+(\\.)\\s+"),
//    mathMode = true,
//    replacement = { _, _ -> "\\cdot" },
//    replacementRange = { it.groupRange(1) },
//    quickFixName = { "Change to \\cdot" }
// )

class LatexCdotInspection : TexifyContextAwareRegexInspectionBase(
    inspectionId = "Cdot",
    regex = Regex.fromLiteral("."),
    highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    applicableContexts = setOf(LatexContexts.Math),
) {
    override fun errorMessage(matcher: MatchResult): String {
        return "\\cdot expected"
    }

    override fun getReplacement(matcher: MatchResult): String {
        return "\\cdot"
    }

    override fun quickFixName(matcher: MatchResult): String {
        return "Change to \\cdot"
    }

    override fun additionalChecks(element: PsiElement): Boolean {
        return element.prevSibling is PsiWhiteSpace && element.nextSibling is PsiWhiteSpace
    }
}