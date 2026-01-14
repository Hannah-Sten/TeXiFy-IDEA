package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.inspections.AbstractTexifyRegexBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts

/**
 * @author Johannes Berger, Li Ernest
 */
class LatexEscapeUnderscoreInspection : AbstractTexifyRegexBasedInspection(
    inspectionId = "EscapeUnderscore",
    regex = Regex.fromLiteral("_"),
    highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    applicableContexts = setOf(
        LatexContexts.Text
    ),
    excludedContexts = setOf(
        LatexContexts.InsideDefinition,
        LatexContexts.Math, // Math mode handles _ correctly.
        LatexContexts.Comment, LatexContexts.MintedFuntimeLand, // Comments and verbatim-like environments do not need escaping.
        LatexContexts.Literal, // just ignore literal blocks
        LatexContexts.LabelDefinition, LatexContexts.LabelReference, LatexContexts.URL, // Label names and URLs may contain _.
    ),
) {
    override fun errorMessage(matcher: MatchResult, context: LContextSet): String = """Escape character \ expected"""

    override fun getReplacement(match: MatchResult, fullElementText: String, project: Project, problemDescriptor: ProblemDescriptor): String = """\_"""

    override fun quickFixName(matcher: MatchResult, contexts: LContextSet): String = """Change to \_"""
}