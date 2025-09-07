package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import nl.hannahsten.texifyidea.inspections.TexifyContextAwareRegexInspectionBase
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexTypes

/**
 * @author Johannes Berger, Li Ernest
 */
class LatexEscapeAmpersandInspection : TexifyContextAwareRegexInspectionBase(
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
    override fun shouldInspectElement(element: PsiElement, lookup: LatexSemanticsLookup): Boolean {
        return element.elementType == LatexTypes.AMPERSAND
    }

    override fun errorMessage(matcher: MatchResult): String {
        return """Escape character \ expected"""
    }

    override fun getReplacement(match: MatchResult, project: Project, problemDescriptor: ProblemDescriptor): String {
        return """\&"""
    }

    override fun quickFixName(matcher: MatchResult): String {
        return """Change to \&"""
    }
}