package nl.hannahsten.texifyidea.inspections.latex.codematurity

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.inspections.AbstractTexifyWholeFileRegexBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet

/**
 * This inspection can be messy if we encounter comments or other false positive `$$`.
 *
 * @author Hannah Schellekens
 */
class LatexPrimitiveEquationInspection : AbstractTexifyWholeFileRegexBasedInspection(
    inspectionId = "PrimitiveEquation",
    regex = """\$\$[\s\S]*?\$\$""".toRegex() // Matches $$...$$ with minimal content in between
) {

    override fun errorMessage(matcher: MatchResult, context: LContextSet): String {
        return "Use '\\[..\\]' instead of primitive TeX display math."
    }

    override fun quickFixName(matcher: MatchResult, contexts: LContextSet): String {
        return "Replace with '\\[..\\]'"
    }

    override fun getReplacement(match: MatchResult, fullElementText: String, project: Project, problemDescriptor: ProblemDescriptor): String {
        return "\\[${match.groupValues[1]}\\]"
    }
}