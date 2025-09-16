package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.inspections.AbstractTexifyRegexBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts

/**
 * @author Hannah Schellekens
 */
// open class LatexGroupedSubSupScriptInspection : TexifyRegexInspection(
//    inspectionDisplayName = "Grouped superscript and subscript",
//    inspectionId = "GroupedSubSupScript",
//    errorMessage = {
//        val subSup = if (it.group(1) == "_") "Sub" else "Super"
//        "${subSup}script is not grouped"
//    },
//    pattern = Pattern.compile("((?<!(\\\\)|(\\\\string))[_^])([a-zA-Z0-9][a-zA-Z0-9]+)"),
//    mathMode = true,
//    highlightRange = { TextRange(it.start() - 1, it.end()) },
//    replacement = { it, _ -> "{${it.group(4)}}" },
//    replacementRange = { it.groupRange(4) },
//    quickFixName = { "Insert curly braces" }
// ) {
//
//    override fun checkContext(element: PsiElement): Boolean {
//        val parent = element.parentOfType(LatexCommands::class)
//        return when (parent?.name) {
//            "\\label", "\\bibitem" -> false
//            else -> super.checkContext(element)
//        }
//    }
// }

class LatexGroupedSubSupScriptInspection : AbstractTexifyRegexBasedInspection(
    inspectionId = "GroupedSubSupScript",
    regex = """(?<!(\\)|(\\string))(?<subsup>[_^])(?<content>[a-zA-Z0-9]{2,})""".toRegex(),
    highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    applicableContexts = setOf(LatexContexts.Math),
) {
    override fun errorMessage(matcher: MatchResult, context: LContextSet): String {
        val subSup = if (matcher.groups["subsup"]?.value == "_") "Sub" else "Super"
        return "${subSup}script is not grouped"
    }

    override fun quickFixName(matcher: MatchResult, contexts: LContextSet): String {
        return "Insert curly braces"
    }

    override fun getHighlightRange(matcher: MatchResult): IntRange {
        return matcher.groups["content"]?.range ?: matcher.range
    }

    override fun getReplacement(match: MatchResult, fullElementText: String, project: Project, problemDescriptor: ProblemDescriptor): String {
        val content = match.groups["content"]?.value ?: ""
        val op = match.groups["subsup"]?.value ?: "_"
        return "$op{$content}"
    }
}