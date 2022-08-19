package nl.hannahsten.texifyidea.inspections.latex.typesetting

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.get
import nl.hannahsten.texifyidea.util.insertUsepackage
import nl.hannahsten.texifyidea.util.requiredParameter
import java.util.regex.Matcher

/**
 * Highlights uses of mathematical symbols composed of a colon with a equality-like relational symbol, where the colon
 * will appear vertically misaligned. The `mathtools` package provides a solution with substituting commands.
 *
 * See also Section 3.7.2 of the `mathtools` package documentation.
 *
 * @author Sten Wessel
 */
open class LatexVerticallyCenteredColonInspection : TexifyRegexInspection(
    inspectionDisplayName = "Vertically uncentered colon",
    inspectionId = "VerticallyCenteredColon",
    errorMessage = { "Colon is vertically uncentered" },
    highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    pattern = REGEX,
    mathMode = true,
    replacement = this::replacement,
    replacementRange = { it.groupRange(0) },
    quickFixName = { "Change to ${PATTERNS[it.group(0).replace(WHITESPACE, "")]!!.command} (mathtools)" },
    cancelIf = { _, file ->
        // Per mathtools documentation, colons are automatically centered when this option is set.
        // It is impossible to determine whether this option is actually set (think scoping, but this option can also be
        // turned of with \mathtoolsset{centercolon=false})
        // Thus, whenever someone fiddles with this, we turn off the inspection to prevent false positives.
        file.commandsInFileSet().any { it.name == "\\mathtoolsset" && it.requiredParameter(0)?.contains("centercolon") == true }
    }
) {

    private data class Pattern(val regex: String, val command: String)

    companion object {

        // Whitespace in between is matched, except for newlines (we have to draw the line somewhere...)
        private val PATTERNS = mapOf(
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

        private val WHITESPACE = """[^\S\r\n]+""".toRegex()

        private val REGEX = PATTERNS.values.joinToString(prefix = "(", separator = "|", postfix = ")") { it.regex }.toPattern()

        fun replacement(matcher: Matcher, file: PsiFile): String {
            val replacement = PATTERNS[matcher.group(0).replace(WHITESPACE, "")]!!.command

            // If the next character would be a letter, it would mess up the command that is inserted: append a space as well
            if (file.document()?.get(matcher.end())?.getOrNull(0)?.isLetter() == true) {
                return "$replacement "
            }

            return replacement
        }
    }

    override fun applyFixes(
        descriptor: ProblemDescriptor,
        replacementRanges: List<IntRange>,
        replacements: List<String>,
        groups: List<List<String>>
    ) {
        super.applyFixes(descriptor, replacementRanges, replacements, groups)

        // We override applyFixes instead of applyFix because all fixes need to be applied together, and only after that we insert any required package.
        val file = descriptor.psiElement.containingFile ?: return
        file.insertUsepackage(LatexPackage.MATHTOOLS)
    }
}