package nl.hannahsten.texifyidea.inspections.latex.typesetting

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.lang.Diacritic
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexMathContent
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.util.parser.hasParent
import nl.hannahsten.texifyidea.util.parser.inMathContext
import nl.hannahsten.texifyidea.util.parser.isComment
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Hannah Schellekens
 */
open class LatexDiacriticIJInspection : TexifyRegexInspection(
    inspectionDisplayName = "Dotless versions of i and j should be used with diacritics",
    inspectionId = "DiacriticIJ",
    highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    errorMessage = { "Diacritic must be placed upon a dotless ${letter(it)}" },
    pattern = Pattern.compile(
        "(${Diacritic.allValues().joinToString("|") {
            it.command.replace("\\", "\\\\")
                .replace("^", "\\^")
                .replace(".", "\\.")
        }})\\{?([ij])}?"
    ),
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

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.COMMAND)!!

    override fun checkContext(matcher: Matcher, element: PsiElement): Boolean {
        if (element.isComment()) return false

        val file = element.containingFile
        val offset = matcher.end()

        val foundAhead = file.findElementAt(offset)
        if (foundAhead is LeafPsiElement && foundAhead.elementType == LatexTypes.COMMAND_TOKEN) {
            return false
        }

        val found = file.findElementAt(offset - 1) ?: return false
        return found.hasParent(LatexNormalText::class) || found.hasParent(LatexMathContent::class)
    }
}
