package nl.rubensten.texifyidea.inspections.latex

import nl.rubensten.texifyidea.inspections.TexifyRegexInspection
import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class LatexXInsteadOfTimesInspection : TexifyRegexInspection(
        inspectionDisplayName = "Use of x instead of \\times",
        inspectionShortName = "XInsteadOfTimes",
        errorMessage = { "\\times expected" },
        pattern = Pattern.compile("[0-9]\\s+(x)\\s+[0-9]"),
        mathMode = true,
        replacement = { _, _ -> "\\times" },
        replacementRange = { it.groupRange(1) },
        quickFixName = { "Change to \\times" }
)