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
        groupFetcher = { listOf(it.group(1)) },
        replacementRange = { it.start(1)..it.start(1) + 1 },
        quickFixName = "Change to \\cdot"
)