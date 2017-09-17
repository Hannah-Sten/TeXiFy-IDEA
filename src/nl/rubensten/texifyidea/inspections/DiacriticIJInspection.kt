package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.lang.Diacritic
import nl.rubensten.texifyidea.psi.LatexMathContent
import nl.rubensten.texifyidea.psi.LatexNormalText
import nl.rubensten.texifyidea.util.hasParent
import nl.rubensten.texifyidea.util.inMathContext
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class DiacriticIJInspection : TexifyRegexInspection(
        inspectionDisplayName = "Dotless versions of i and j should be used with diacritics",
        inspectionShortName = "DiacriticIJ",
        highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
        errorMessage = { "Diacritic must be placed upon a dotless ${letter(it)}" },
        pattern = Pattern.compile("(${Diacritic.allValues().joinToString("|") {
            it.command.replace("\\", "\\\\")
                    .replace("^", "\\^")
                    .replace(".", "\\.")
        }})\\{?([ij])}?"),
        replacement = this::replacement,
        replacementRange = this::replaceRange,
        quickFixName = { "Change to dotless ${letter(it)}" }
) {

    companion object {

        fun replacement(it: Matcher, file: PsiFile): String {
            val group = it.group(2)
            val element = file.findElementAt(it.start())

            // Math mode.
            if (element != null && element.inMathContext()) {
                return when (group) {
                    "i" -> "\\imath"
                    else -> "\\jmath"
                }
            }

            // Regular text.
            return when (it.group(2)) {
                "i" -> "{\\i}"
                else -> "{\\j}"
            }
        }

        fun replaceRange(it: Matcher) = it.groupRange(2)

        fun letter(it: Matcher) = it.group(2)!!
    }

    override fun checkContext(matcher: Matcher, element: PsiElement): Boolean {
        val file = element.containingFile
        val found = file.findElementAt(matcher.end() - 1) ?: return false
        return found.hasParent(LatexNormalText::class) || found.hasParent(LatexMathContent::class)
    }
}