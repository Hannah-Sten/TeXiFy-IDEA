package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.ProblemHighlightType
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import java.util.regex.Pattern

/**
 * @author Michael Milton
 */
class LatexQuoteInspection : TexifyRegexInspection(
        inspectionDisplayName = "Incorrect quotation",
        inspectionId = "AsciiQuotes",
        errorMessage = { """"ASCII quotes" were used instead of ``LaTeX quotes''""" },
        highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
        pattern = Pattern.compile("""".+?"|'.+?'"""),
        replacement = { matcher, _ ->
            val match = matcher.group(0)
            val stripped = match.slice(1..match.length - 2)
            if (match[0] == '"') {
                "``$stripped''"
            }
            else {
                "`$stripped'"
            }
        },
        quickFixName = { """Change to ``LaTeX quotes''""" }
) {}
