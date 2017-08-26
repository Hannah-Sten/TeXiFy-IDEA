package nl.rubensten.texifyidea.inspections

import nl.rubensten.texifyidea.util.toTextRange
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class RedundantParInspection : TexifyRegexInspection(
        inspectionDisplayName = "Redundant use of \\par",
        inspectionShortName = "RedundantPar",
        errorMessage = { "Use of \\par is redundant here" },
        pattern = Pattern.compile("((\\s*\\n\\s*\\n\\s*(\\\\par))|(\\n\\s*(\\\\par)\\s*\\n)|((\\\\par)\\s*\\n\\s*\\n))"),
        replacement = { "" },
        replacementRange = this::parRange,
        quickFixName = { "Remove \\par" },
        highlightRange = { parRange(it).toTextRange() }
) {

    companion object {

        fun parRange(it: Matcher) = if (it.group(3) != null) {
            it.groupRange(3)
        }
        else if (it.group(5) != null) {
            it.groupRange(5)
        }
        else {
            it.groupRange(7)
        }
    }
}