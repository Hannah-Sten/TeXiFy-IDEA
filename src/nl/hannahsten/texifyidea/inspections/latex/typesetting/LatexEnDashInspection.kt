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
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.magic.PatternMagic

class LatexEnDashInspection : AbstractTexifyRegexBasedInspection(
    inspectionId = "EnDash",
    regex = Regex("(?<![0-9\\-])\\s+(([0-9]+)\\s*[\\- ]+\\s*([0-9]+))(\\s+|.)(?=[^0-9\\-])"),
    highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    applicableContexts = setOf(LatexContexts.Text)
) {
    override fun errorMessage(matcher: MatchResult, context: LContextSet): String = "En dash expected"

    override fun quickFixName(matcher: MatchResult, contexts: LContextSet): String = "Convert to en dash"

    override fun getReplacement(match: MatchResult, fullElementText: String, project: Project, problemDescriptor: ProblemDescriptor): String = "${match.groupValues[2]}--${match.groupValues[3]}"

    override fun getHighlightRange(matcher: MatchResult): IntRange = matcher.groups[1]?.range ?: matcher.range

    override fun additionalChecks(element: PsiElement, match: MatchResult, bundle: DefinitionBundle, file: PsiFile): Boolean {
        val group1 = match.groups[1]?.value ?: return false
        return !PatternMagic.correctEnDash.matcher(group1).matches()
    }

    override fun shouldInspectElement(element: PsiElement, lookup: LatexSemanticsLookup): Boolean = element is LatexNormalText

    override fun shouldInspectChildrenOf(element: PsiElement, state: LContextSet, lookup: LatexSemanticsLookup): Boolean {
        if (element is LatexNormalText) return false
        return super.shouldInspectChildrenOf(element, state, lookup)
    }
}
