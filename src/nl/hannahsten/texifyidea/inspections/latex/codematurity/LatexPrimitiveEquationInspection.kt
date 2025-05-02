package nl.hannahsten.texifyidea.inspections.latex.codematurity

import com.intellij.codeInspection.ProblemHighlightType
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.util.toTextRange
import java.util.regex.Pattern

/**
 * @author Hannah Schellekens
 */
open class LatexPrimitiveEquationInspection : TexifyRegexInspection(
    inspectionDisplayName = "Discouraged use of primitive TeX display math",
    inspectionId = "PrimitiveEquation",
    errorMessage = { "Use '\\[..\\]' instead of primitive TeX display math." },
    pattern = Pattern.compile("\\\$\\\$([^\$]*\\\$?[^\$]*)\\\$\\\$"),
    mathMode = false,
    replacement = { matcher, _ -> "\\[${matcher.group(1)}\\]" },
    replacementRange = { it.groupRange(0) },
    highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    highlightRange = { it.groupRange(0).toTextRange() },
    quickFixName = { "Replace with '\\[..\\]'" }
)