package nl.hannahsten.texifyidea.inspections.latex.typesetting

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.inspections.TexifyContextAwareRegexInspectionBase
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.util.parser.findNextAdjacentWhiteSpace
import nl.hannahsten.texifyidea.util.parser.findPrevAdjacentWhiteSpace

/**
 * @author Hannah Schellekens
 */
class LatexCdotInspection : TexifyContextAwareRegexInspectionBase(
    inspectionId = "Cdot",
    regex = Regex.fromLiteral("."),
    highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    applicableContexts = setOf(LatexContexts.Math),
) {
    override fun errorMessage(matcher: MatchResult): String {
        return "\\cdot expected"
    }

    override fun getReplacement(match: MatchResult, project: Project, problemDescriptor: ProblemDescriptor): String {
        return "\\cdot"
    }

    override fun quickFixName(matcher: MatchResult): String {
        return "Change to \\cdot"
    }

    override fun additionalChecks(element: PsiElement, text: String, match: MatchResult): Boolean {
        return element.findPrevAdjacentWhiteSpace() != null && element.findNextAdjacentWhiteSpace() != null
    }
}