package nl.hannahsten.texifyidea.inspections.latex.typesetting

import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.toTextRange
import java.util.regex.Pattern

/**
 * @author Hannah Schellekens
 */
open class LatexEnDashInspection : TexifyRegexInspection(
    inspectionDisplayName = "En dash in number ranges",
    inspectionId = "EnDash",
    errorMessage = { "En dash expected" },
    pattern = Pattern.compile("(?<![0-9\\-])\\s+(([0-9]+)\\s*[\\- ]+\\s*([0-9]+))(\\s+|.)(?=[^0-9\\-])")!!,
    quickFixName = { "Convert to en dash" },
    cancelIf = { matcher, _ -> PatternMagic.correctEnDash.matcher(matcher.group(1)).matches() },
    replacementRange = { it.groupRange(1) },
    replacement = { matcher, _ -> "${matcher.group(2)}--${matcher.group(3)}" },
    highlightRange = { it.groupRange(1).toTextRange() },
    groupFetcher = { listOf(it.group(2), it.group(3)) }
)