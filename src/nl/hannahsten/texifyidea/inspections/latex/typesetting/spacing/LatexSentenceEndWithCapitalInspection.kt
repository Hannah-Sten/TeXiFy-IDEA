package nl.hannahsten.texifyidea.inspections.latex.typesetting.spacing

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.inspections.AbstractTexifyRegexBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.util.parser.findNextAdjacentWhiteSpace

/**
 * @author Hannah Schellekens
 */
class LatexSentenceEndWithCapitalInspection : AbstractTexifyRegexBasedInspection(
    inspectionId = "SentenceEndWithCapital",
    regex = """(?<=[A-ZÀ-Ý])\.""".toRegex(),
    applicableContexts = setOf(LatexContexts.Text)
) {
    override fun errorMessage(matcher: MatchResult, context: LContextSet): String {
        return "Sentences ending with a capital letter should end with an end-of-sentence space"
    }

    override fun quickFixName(matcher: MatchResult, contexts: LContextSet): String {
        return "Add an end-of-sentence space"
    }

    override fun getReplacement(match: MatchResult, fullElementText: String, project: Project, problemDescriptor: ProblemDescriptor): String {
        return "\\@."
    }

    override fun additionalChecks(element: PsiElement, match: MatchResult, bundle: DefinitionBundle, file: PsiFile): Boolean {
        val nextWhiteSpace = element.findNextAdjacentWhiteSpace() ?: return false
        // Check if there is a newline in the whitespace
        return nextWhiteSpace.textContains('\n')
    }
}