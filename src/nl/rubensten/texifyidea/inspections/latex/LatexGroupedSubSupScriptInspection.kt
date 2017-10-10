package nl.rubensten.texifyidea.inspections.latex

import com.intellij.openapi.util.TextRange
import nl.rubensten.texifyidea.inspections.TexifyRegexInspection
import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class LatexGroupedSubSupScriptInspection : TexifyRegexInspection(
        inspectionDisplayName = "Grouped superscript and subscript",
        myInspectionId = "GroupedSubSupScript",
        errorMessage = {
            val subSup = if (it.group(1) == "_") "Sub" else "Super"
            "${subSup}script is not grouped"
        },
        pattern = Pattern.compile("([_^])([a-zA-Z0-9][a-zA-Z0-9]+)"),
        mathMode = true,
        highlightRange = { TextRange(it.start() - 1, it.end()) },
        replacement = { it, _ -> "{${it.group(2)}}" },
        replacementRange = { it.groupRange(2) },
        quickFixName = { "Insert curly braces" }
)