package nl.hannahsten.texifyidea.inspections.latex.typesetting.spacing

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.inspections.AbstractTexifyRegexBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexLib.Companion.AMSSYMB
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.insertUsepackage

/**
 * @author Hannah Schellekens
 */
class LatexExtremeInequalityInspection : AbstractTexifyRegexBasedInspection(
    inspectionId = "ExtremeInequality",
    regex = """
        (<{2,3}|>{2,3})(.?)
    """.trimIndent().toRegex(),
    applicableContexts = setOf(LatexContexts.Math)
) {
    override fun errorMessage(matcher: MatchResult, context: LContextSet): String = "Use the amssymb symbol instead."

    override fun quickFixName(matcher: MatchResult, contexts: LContextSet): String = "Insert amssymb symbol."

    override fun getHighlightRange(matcher: MatchResult): IntRange = matcher.groups[1]!!.range

    override fun shouldInspectElement(element: PsiElement, lookup: LatexSemanticsLookup): Boolean = element is LatexNormalText

    override fun shouldInspectChildrenOf(element: PsiElement, state: LContextSet, lookup: LatexSemanticsLookup): Boolean = !shouldInspectElement(element, lookup)

    override fun getReplacement(match: MatchResult, fullElementText: String, project: Project, problemDescriptor: ProblemDescriptor): String {
        val rep = when (val operator = match.groups[1]!!.value) {
            "<<" -> "\\ll"
            "<<<" -> "\\lll"
            ">>" -> "\\gg"
            ">>>" -> "\\ggg"
            else -> operator
        }
        val char = match.groups[2]?.value ?: ""
        return if (char.isNotBlank()) {
            "$rep $char"
        }
        else {
            rep
        }
    }

    override fun doApplyFix(project: Project, descriptor: ProblemDescriptor, match: MatchResult, fullElementText: String) {
        super.doApplyFix(project, descriptor, match, fullElementText)
        val file = descriptor.psiElement.containingFile ?: return
        file.insertUsepackage(AMSSYMB)
    }
}