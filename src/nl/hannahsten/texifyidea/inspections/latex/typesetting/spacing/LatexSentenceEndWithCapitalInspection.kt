package nl.hannahsten.texifyidea.inspections.latex.typesetting.spacing

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
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

    override fun getReplacement(match: MatchResult, project: Project, problemDescriptor: ProblemDescriptor): String {
        return "\\@."
    }

    override fun additionalChecks(element: PsiElement, match: MatchResult): Boolean {
        val nextWhiteSpace = element.findNextAdjacentWhiteSpace() ?: return false
        // Check if there is a newline in the whitespace
        return nextWhiteSpace.textContains('\n')
    }
}

// open class LatexSentenceEndWithCapitalInspection : TexifyRegexInspection(
//    inspectionDisplayName = "End-of-sentence space after sentences ending with capitals",
//    inspectionId = "SentenceEndWithCapital",
//    errorMessage = { "Sentences ending with a capital letter should end with an end-of-sentence space" },
//    pattern = Pattern.compile("[A-ZÀ-Ý](\\.)[ \\t]*\\n"),
//    replacement = { _, _ -> "\\@." },
//    replacementRange = { it.start(1)..it.start(1) + 1 },
//    quickFixName = { "Add an end-of-sentence space" },
//    cancelIf = { matcher, file -> isInElement<LatexCommands>(matcher, file) }
// ) {
//
//    override fun checkContext(matcher: Matcher, element: PsiElement): Boolean {
//        if (element.inDirectEnvironment(setOf("verbatim", "verbatim*", "lstlisting"))) {
//            return false
//        }
//        return super.checkContext(matcher, element)
//    }
// }
