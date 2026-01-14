package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import nl.hannahsten.texifyidea.inspections.AbstractTexifyRegexBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexTypes

/**
 * @author Johannes Berger, Li Ernest
 */
class LatexEscapeAmpersandInspection : AbstractTexifyRegexBasedInspection(
    inspectionId = "EscapeAmpersand",
    regex = Regex.fromLiteral("&"),
    highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    excludedContexts = setOf(
        LatexContexts.Tabular, LatexContexts.Alignable, // Tabular and align environments handle & correctly.
        LatexContexts.Comment, LatexContexts.MintedFuntimeLand, // Comments and verbatim-like environments do not need escaping.
        LatexContexts.Literal, // just ignore literal blocks
        LatexContexts.LabelReference, LatexContexts.LabelDefinition // Label names and URLs may contain &.
    )
) {
    override fun shouldInspectElement(element: PsiElement, lookup: LatexSemanticsLookup): Boolean = element.elementType == LatexTypes.AMPERSAND

    override fun errorMessage(matcher: MatchResult, context: LContextSet): String = """Escape character \ expected"""

    override fun getReplacement(match: MatchResult, fullElementText: String, project: Project, problemDescriptor: ProblemDescriptor): String = """\&"""

    override fun quickFixName(matcher: MatchResult, contexts: LContextSet): String = """Change to \&"""
}