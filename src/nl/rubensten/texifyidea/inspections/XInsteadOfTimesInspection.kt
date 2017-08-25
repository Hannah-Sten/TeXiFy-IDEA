package nl.rubensten.texifyidea.inspections

import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class XInsteadOfTimesInspection : TexifyRegexInspection(
        inspectionDisplayName = "Use of x instead of \\times",
        inspectionShortName = "XInsteadOfTimes",
        errorMessage = { "\\times expected" },
        pattern = Pattern.compile("[0-9]\\s+(x)\\s+[0-9]"),
        mathMode = true,
        replacement = { "\\times" },
        replacementRange = { it.groupRange(1) },
        quickFixName = { "Change to \\times" }
)