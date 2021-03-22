package nl.hannahsten.texifyidea.inspections.latex.codestyle

import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import java.util.regex.Pattern

/**
 * @author Hannah Schellekens
 */
open class LatexXInsteadOfTimesInspection : TexifyRegexInspection(
    inspectionDisplayName = "Use of x instead of \\times",
    inspectionId = "XInsteadOfTimes",
    errorMessage = { "\\times expected" },
    pattern = Pattern.compile("[0-9]\\s+(x)\\s+[0-9]"),
    mathMode = true,
    replacement = { _, _ -> "\\times" },
    replacementRange = { it.groupRange(1) },
    quickFixName = { "Change to \\times" }
)