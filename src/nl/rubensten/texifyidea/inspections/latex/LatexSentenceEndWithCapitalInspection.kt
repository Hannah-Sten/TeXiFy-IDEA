package nl.rubensten.texifyidea.inspections.latex

import com.intellij.psi.PsiElement
import nl.rubensten.texifyidea.inspections.TexifyRegexInspection
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.inDirectEnvironment
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class LatexSentenceEndWithCapitalInspection : TexifyRegexInspection(
        inspectionDisplayName = "End-of-sentence space after sentences ending with capitals",
        myInspectionId = "SentenceEndWithCapital",
        errorMessage = { "Sentences ending with a capital letter should end with an end-of-sentence space" },
        pattern = Pattern.compile("[A-ZÀ-Ý](\\.)[ \\t]*\\n"),
        replacement = { _, _ -> "\\@." },
        replacementRange = { it.start(1)..it.start(1) + 1 },
        quickFixName = { "Add an end-of-sentence space" },
        cancelIf = { matcher, file -> isInElement<LatexCommands>(matcher, file) }
) {

    override fun checkContext(matcher: Matcher, element: PsiElement): Boolean {
        if (element.inDirectEnvironment(setOf("verbatim", "verbatim*", "lstlisting"))) {
            return false
        }
        return super.checkContext(matcher, element)
    }
}