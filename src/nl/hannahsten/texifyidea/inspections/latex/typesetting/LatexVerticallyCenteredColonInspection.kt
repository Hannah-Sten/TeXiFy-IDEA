package nl.hannahsten.texifyidea.inspections.latex.typesetting

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.inspections.AbstractTexifyRegexBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexMathEnvironment
import nl.hannahsten.texifyidea.util.insertUsepackage
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil

/**
 * Highlights uses of mathematical symbols composed of a colon with a equality-like relational symbol, where the colon
 * will appear vertically misaligned. The `mathtools` package provides a solution with substituting commands.
 *
 * See also Section 3.7.2 of the `mathtools` package documentation.
 *
 * @author Sten Wessel
 */

class LatexVerticallyCenteredColonInspection : AbstractTexifyRegexBasedInspection(
    inspectionId = "VerticallyCenteredColon",
    regex = Util.REGEX,
    highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
) {
    override fun errorMessage(matcher: MatchResult, context: LContextSet): String = "Colon is vertically uncentered"

    override fun quickFixName(matcher: MatchResult, contexts: LContextSet): String {
        val key = Util.PATTERNS.keys.firstOrNull { matcher.value.replace(Util.WHITESPACE, "") == it }
        val command = key?.let { Util.PATTERNS[it]?.command } ?: ""
        return "Change to $command (mathtools)"
    }

    override fun getHighlightRange(matcher: MatchResult): IntRange = matcher.range

    override fun shouldInspectElement(element: PsiElement, lookup: LatexSemanticsLookup): Boolean = element is LatexMathEnvironment ||
        (element is LatexEnvironment && LatexPsiUtil.isContextIntroduced(element, lookup, LatexContexts.Math))

    override fun shouldInspectChildrenOf(element: PsiElement, state: LContextSet, lookup: LatexSemanticsLookup): Boolean = !shouldInspectElement(element, lookup)

    override fun getReplacement(match: MatchResult, fullElementText: String, project: Project, problemDescriptor: ProblemDescriptor): String {
        val key = Util.PATTERNS.keys.firstOrNull { match.value.replace(Util.WHITESPACE, "") == it }
        val replacement = key?.let { Util.PATTERNS[it]?.command } ?: match.value
        val end = match.range.last + 1
        val nextChar = fullElementText.getOrNull(end)
        return if (nextChar?.isLetter() == true) "$replacement " else replacement
    }

    override fun doApplyFix(project: Project, descriptor: ProblemDescriptor, match: MatchResult, fullElementText: String) {
        super.doApplyFix(project, descriptor, match, fullElementText)
        val file = descriptor.psiElement.containingFile ?: return
        file.insertUsepackage(LatexLib.MATHTOOLS)
    }

    override fun prepareInspectionForFile(file: PsiFile, bundle: DefinitionBundle): Boolean {
        val hasCenterColon = NewCommandsIndex.getByNameInFileSet("\\mathtoolsset", file)
            .any { it.requiredParameterText(0)?.contains("centercolon") == true }
        return !hasCenterColon
    }

    object Util {
        internal data class Pattern(val regex: String, val command: String)
        internal val PATTERNS = mapOf(
            ":=" to Pattern(""":[^\S\r\n]*=""", "\\coloneqq"),
            "::=" to Pattern(""":[^\S\r\n]*:[^\S\r\n]*=""", "\\Coloneqq"),
            ":-" to Pattern(""":[^\S\r\n]*-""", "\\coloneq"),
            "::-" to Pattern(""":[^\S\r\n]*:[^\S\r\n]*-""", "\\Coloneq"),
            "=:" to Pattern("""=[^\S\r\n]*:""", "\\eqqcolon"),
            "=::" to Pattern("""=[^\S\r\n]*:[^\S\r\n]*:""", "\\Eqqcolon"),
            "-:" to Pattern("""-[^\S\r\n]*:""", "\\eqcolon"),
            "-::" to Pattern("""-[^\S\r\n]*:[^\S\r\n]*:""", "\\Eqcolon"),
            ":\\approx" to Pattern(""":[^\S\r\n]*\\approx(?![a-zA-Z])""", "\\colonapprox"),
            "::\\approx" to Pattern(""":[^\S\r\n]*:[^\S\r\n]*\\approx(?![a-zA-Z])""", "\\Colonapprox"),
            ":\\sim" to Pattern(""":[^\S\r\n]*\\sim(?![a-zA-Z])""", "\\colonsim"),
            "::\\sim" to Pattern(""":[^\S\r\n]*:[^\S\r\n]*\\sim(?![a-zA-Z])""", "\\Colonsim"),
            "::" to Pattern(""":[^\S\r\n]*:""", "\\dblcolon"),
        )
        internal val WHITESPACE = """[^\S\r\n]+""".toRegex()
        internal val REGEX = PATTERNS.values.joinToString(prefix = "(", separator = "|", postfix = ")") { it.regex }.toRegex()
    }
}