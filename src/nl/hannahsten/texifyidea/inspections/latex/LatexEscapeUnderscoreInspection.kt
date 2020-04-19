package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.ProblemHighlightType
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import java.util.regex.Pattern

/**
 * @author Johannes Berger
 */
class LatexEscapeUnderscoreInspection : TexifyRegexInspection(
        inspectionDisplayName = "Unescaped _ character",
        inspectionId = "LatexEscapeUnderscore",
        errorMessage = { "Escape character \\ expected" },
        highlight = ProblemHighlightType.WARNING,
        pattern = Pattern.compile("""(?<!\\)_"""),
        replacement = { _, _ -> """\_""" },
        quickFixName = { """Change to \_""" }
)