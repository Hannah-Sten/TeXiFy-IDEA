package nl.hannahsten.texifyidea.inspections.latex.typesetting

import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import java.util.regex.Pattern

/**
 * @author Hannah Schellekens
 */
open class LatexCdotInspection : TexifyRegexInspection(
    inspectionDisplayName = "Use of . instead of \\cdot",
    inspectionId = "Cdot",
    errorMessage = { "\\cdot expected" },
    pattern = Pattern.compile("\\s+(\\.)\\s+"),
    mathMode = true,
    replacement = { _, _ -> "\\cdot" },
    replacementRange = { it.groupRange(1) },
    quickFixName = { "Change to \\cdot" }
)