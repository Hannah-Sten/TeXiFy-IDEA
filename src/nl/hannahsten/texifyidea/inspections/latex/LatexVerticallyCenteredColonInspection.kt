package nl.hannahsten.texifyidea.inspections.latex

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
import java.util.regex.Pattern

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
    pattern = PATTERNS.values.joinToString(prefix = "(", separator = "|", postfix = ")") { it.regex }.toPattern(),
    mathMode = true,
    replacement = this::replacement,
    replacementRange = { it.groupRange(0) },
    quickFixName = { "Change to ${PATTERNS[it.group(0)]!!.command} (mathtools)" },
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

        private val PATTERNS = mapOf(
            ":=" to Pattern(""":=""", "\\coloneqq"),
            "::=" to Pattern("""::=""", "\\Coloneqq"),
            ":-" to Pattern(""":-""", "\\coloneq"),
            "::-" to Pattern("""::-""", "\\Coloneq"),
            "=:" to Pattern("""=:""", "\\eqqcolon"),
            "=::" to Pattern("""=::""", "\\Eqqcolon"),
            "-:" to Pattern("""-:""", "\\eqcolon"),
            "-::" to Pattern("""-::""", "\\Eqcolon"),
            ":\\approx" to Pattern(""":\\approx(?![a-zA-Z])""", "\\colonapprox"),
            "::\\approx" to Pattern("""::\\approx(?![a-zA-Z])""", "\\Colonapprox"),
            ":\\sim" to Pattern(""":\\sim(?![a-zA-Z])""", "\\colonsim"),
            "::\\sim" to Pattern("""::\\sim(?![a-zA-Z])""", "\\Colonsim"),
            "::" to Pattern("""::""", "\\dblcolon"),
        )

        fun replacement(matcher: Matcher, file: PsiFile): String {
            val replacement = PATTERNS[matcher.group(0)]!!.command

            // If the next character would be a letter, it would mess up the command that is inserted: append a space as well
            if (file.document()?.get(matcher.end())?.get(0)?.isLetter() == true) {
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