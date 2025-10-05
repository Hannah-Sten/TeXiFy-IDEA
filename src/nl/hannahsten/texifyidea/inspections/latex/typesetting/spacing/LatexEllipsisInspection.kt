package nl.hannahsten.texifyidea.inspections.latex.typesetting.spacing

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.inspections.AbstractTexifyRegexBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.AMSMATH
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil

class LatexEllipsisInspection : AbstractTexifyRegexBasedInspection(
    inspectionId = "Ellipsis",
    regex = """(?<!\.)(\.\.\.)(?!\.)""".toRegex(),
    applicableContexts = setOf(LatexContexts.Text, LatexContexts.Math),
    excludedContexts = setOf(LatexContexts.TikzPicture)
    // we should have excluded other contexts, but we explicitly exclude this in case
) {
    override fun errorMessage(matcher: MatchResult, context: LContextSet): String {
        return "Ellipsis with ... instead of \\ldots or \\dots"
    }

    override fun quickFixName(matcher: MatchResult, contexts: LContextSet): String {
        return if (LatexContexts.Math in contexts) {
            "Convert to \\dots (amsmath package)"
        }
        else {
            "Convert to \\ldots"
        }
    }

    override fun getReplacement(match: MatchResult, fullElementText: String, project: Project, problemDescriptor: ProblemDescriptor): String {
        val lookup = LatexDefinitionService.getInstance(project).getDefBundlesMerged(problemDescriptor.psiElement.containingFile)
        return if (LatexPsiUtil.isInsideContext(problemDescriptor.psiElement, LatexContexts.Math, lookup)) {
            "\\dots"
        }
        else {
            "\\ldots"
        }
    }

    override fun doApplyFix(project: Project, descriptor: ProblemDescriptor, match: MatchResult, fullElementText: String) {
        val element = descriptor.psiElement
        val document = element.containingFile.document() ?: return
        val repRange = match.range.toTextRange().shiftRight(element.startOffset)
        val rep = getReplacement(match, fullElementText, project, descriptor)
        document.replaceString(repRange, rep)
        if (rep == "\\dots") {
            element.containingFile.insertUsepackage(AMSMATH)
        }
    }

    override fun shouldInspectElement(element: PsiElement, lookup: LatexSemanticsLookup): Boolean {
        return element is LatexNormalText
    }

    override fun shouldInspectChildrenOf(element: PsiElement, state: LContextSet, lookup: LatexSemanticsLookup): Boolean {
        return !shouldInspectElement(element, lookup)
    }
}
