package nl.hannahsten.texifyidea.inspections.latex.typesetting

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.inspections.AbstractTexifyRegexBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.util.parser.findNextAdjacentWhiteSpace
import nl.hannahsten.texifyidea.util.parser.findPrevAdjacentWhiteSpace

class LatexXInsteadOfTimesInspection : AbstractTexifyRegexBasedInspection(
    inspectionId = "XInsteadOfTimes",
    regex = Regex("^x$", RegexOption.IGNORE_CASE),
    highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    applicableContexts = setOf(LatexContexts.Math),
) {
    override fun errorMessage(matcher: MatchResult, context: LContextSet): String = "\\times expected"

    override fun getReplacement(match: MatchResult, fullElementText: String, project: Project, problemDescriptor: ProblemDescriptor): String = "\\times"

    override fun quickFixName(matcher: MatchResult, contexts: LContextSet): String = "Change to \\times"

    override fun additionalChecks(element: PsiElement, match: MatchResult, bundle: DefinitionBundle, file: PsiFile): Boolean {
        // inspection only triggers when x is surrounded by whitespace and both sides are numbers
        val prev = element.findPrevAdjacentWhiteSpace()?.prevSibling
        val next = element.findNextAdjacentWhiteSpace()?.nextSibling
        return prev != null && next != null && prev.text.matches(Regex("\\d+")) && next.text.matches(Regex("\\d+"))
    }
}
