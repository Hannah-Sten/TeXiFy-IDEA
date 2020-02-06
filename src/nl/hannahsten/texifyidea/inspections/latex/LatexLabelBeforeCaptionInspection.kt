package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import java.util.regex.Pattern

class LatexLabelBeforeCaptionInspection : TexifyRegexInspection(
        inspectionDisplayName = "Label before caption",
        inspectionId = "LabelBeforeCaption",
        errorMessage = { "A label should come after the caption" },
        pattern = Pattern.compile("(\\\\label\\{.*})(\\s*)(\\\\caption\\{.*})"),
        quickFixName = { "Swap label and caption" },
        replacement = { it, _ -> it.group(3) + it.group(2) + it.group(1) }
)