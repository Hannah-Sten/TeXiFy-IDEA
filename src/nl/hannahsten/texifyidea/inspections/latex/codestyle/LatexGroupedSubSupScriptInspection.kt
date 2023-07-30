package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.parser.parentOfType
import java.util.regex.Pattern

/**
 * @author Hannah Schellekens
 */
open class LatexGroupedSubSupScriptInspection : TexifyRegexInspection(
    inspectionDisplayName = "Grouped superscript and subscript",
    inspectionId = "GroupedSubSupScript",
    errorMessage = {
        val subSup = if (it.group(1) == "_") "Sub" else "Super"
        "${subSup}script is not grouped"
    },
    pattern = Pattern.compile("((?<!(\\\\)|(\\\\string))[_^])([a-zA-Z0-9][a-zA-Z0-9]+)"),
    mathMode = true,
    highlightRange = { TextRange(it.start() - 1, it.end()) },
    replacement = { it, _ -> "{${it.group(4)}}" },
    replacementRange = { it.groupRange(4) },
    quickFixName = { "Insert curly braces" }
) {

    override fun checkContext(element: PsiElement): Boolean {
        val parent = element.parentOfType(LatexCommands::class)
        return when (parent?.name) {
            "\\label", "\\bibitem" -> false
            else -> super.checkContext(element)
        }
    }
}