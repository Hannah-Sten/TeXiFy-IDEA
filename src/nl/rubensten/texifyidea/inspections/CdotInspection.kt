package nl.rubensten.texifyidea.inspections

import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class CdotInspection : TexifyRegexInspection(
        inspectionDisplayName = "Use of . instead of \\cdot",
        inspectionShortName = "Cdot",
        errorMessage = "\\cdot expected",
        pattern = Pattern.compile("\\s+(\\.)\\s+"),
        mathMode = true,
        replacement = "\\cdot",
        replacementRange = { it.groupRange(1) },
        quickFixName = "Change to \\cdot"
)