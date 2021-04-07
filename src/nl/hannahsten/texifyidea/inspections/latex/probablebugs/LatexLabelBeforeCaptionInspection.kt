package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import java.util.regex.Pattern

class LatexLabelBeforeCaptionInspection : TexifyRegexInspection(
    inspectionDisplayName = "Label before caption",
    inspectionId = "LabelBeforeCaption",
    errorMessage = { "A label should come after the caption" },
    // Match label before caption, but not if a label follows the caption, because then the label before the caption
    // probably does not intend to label the caption
    pattern = Pattern.compile("(\\\\label\\{[^\\}]*\\})(\\s*)(\\\\caption\\{[^\\}]*\\})(?!\\s*\\\\label)"),
    quickFixName = { "Swap label and caption" },
    replacement = { it, _ -> it.group(3) + it.group(2) + it.group(1) }
)