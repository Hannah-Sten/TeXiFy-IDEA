package nl.hannahsten.texifyidea.inspections.latex.typesetting.spacing

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.inspections.AbstractTexifyRegexBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.AMSSYMB
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.insertUsepackage

/**
 * @author Hannah Schellekens
 */
class LatexExtremeInequalityInspection : AbstractTexifyRegexBasedInspection(
    inspectionId = "ExtremeInequality",
    regex = """
        (<{2,3}|>{2,3})(.?)
    """.trimIndent().toRegex(),
    applicableContexts = setOf(LatexContexts.Math)
) {
    override fun errorMessage(matcher: MatchResult, context: LContextSet): String {
        return "Use the amssymb symbol instead."
    }

    override fun quickFixName(matcher: MatchResult, contexts: LContextSet): String {
        return "Insert amssymb symbol."
    }

    override fun getHighlightRange(matcher: MatchResult): IntRange {
        return matcher.groups[1]!!.range
    }

    override fun shouldInspectElement(element: PsiElement, lookup: LatexSemanticsLookup): Boolean {
        return element is LatexNormalText
    }

    override fun shouldInspectChildrenOf(element: PsiElement, state: LContextSet, lookup: LatexSemanticsLookup): Boolean {
        return !shouldInspectElement(element, lookup)
    }

    override fun getReplacement(match: MatchResult, project: Project, problemDescriptor: ProblemDescriptor): String {
        val rep = when (val operator = match.groups[1]!!.value) {
            "<<" -> "\\ll"
            "<<<" -> "\\lll"
            ">>" -> "\\gg"
            ">>>" -> "\\ggg"
            else -> operator
        }
        val char = match.groups[2]?.value ?: ""
        return if (char.isNotBlank()) {
            "$rep $char"
        }
        else {
            rep
        }
    }

    override fun doApplyFix(project: Project, descriptor: ProblemDescriptor, match: MatchResult) {
        super.doApplyFix(project, descriptor, match)
        val file = descriptor.psiElement.containingFile ?: return
        file.insertUsepackage(AMSSYMB)
        return
    }
}
// open class LatexExtremeInequalityInspection : TexifyRegexInspection(
//    inspectionDisplayName = "Use the matching amssymb symbol for inequalities",
//    inspectionId = "ExtremeInequality",
//    errorMessage = { "Use the amssymb symbol instead." },
//    pattern = Pattern.compile("(<\\s*<(?!\\s*<))|(<\\s*<\\s*<)|(>\\s*>(?!\\s*>))|(>\\s*>\\s*>)"),
//    mathMode = true,
//    replacement = { it, _ -> Util.replacement(it) },
//    replacementRange = Util::replaceRange,
//    quickFixName = { "Insert amssymb symbol." }
// ) {
//
//    object Util {
//        /**
//         * Determines what the replacement for the quick fix must be.
//         */
//        fun replacement(it: Matcher) = if (it.group(1) != null) {
//            "\\ll"
//        }
//        else if (it.group(2) != null) {
//            "\\lll"
//        }
//        else if (it.group(3) != null) {
//            "\\gg"
//        }
//        else "\\ggg"
//
//        /**
//         * Determines what range must be replaced.
//         */
//        fun replaceRange(it: Matcher): IntRange {
//            for (i in 1..4) {
//                if (it.group(i) != null) {
//                    return it.groupRange(i)
//                }
//            }
//
//            return IntRange.EMPTY
//        }
//    }
//
//    override fun applyFixes(descriptor: ProblemDescriptor, replacementRanges: List<IntRange>, replacements: List<String>, groups: List<List<String>>) {
//        super.applyFixes(descriptor, replacementRanges, replacements, groups)
//
//        // We overrided applyFixes instead of applyFix because all fixes need to be applied together, and only after that we insert any required package.
//        val file = descriptor.psiElement.containingFile ?: return
//        file.insertUsepackage(AMSSYMB)
//    }
// }