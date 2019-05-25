package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import nl.rubensten.texifyidea.inspections.TexifyRegexInspection
import nl.rubensten.texifyidea.lang.Package.Companion.AMSSYMB
import nl.rubensten.texifyidea.util.insertUsepackage
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class LatexExtremeInequalityInspection : TexifyRegexInspection(
        inspectionDisplayName = "Use the matching amssymb symbol for inequalities",
        myInspectionId = "ExtremeInequality",
        errorMessage = { "Use the amssymb symbol instead." },
        pattern = Pattern.compile("(<\\s*<(?!\\s*<))|(<\\s*<\\s*<)|(>\\s*>(?!\\s*>))|(>\\s*>\\s*>)"),
        mathMode = true,
        replacement = { it, _ -> replacement(it) },
        replacementRange = this::replaceRange,
        quickFixName = { "Insert amssymb symbol." }
) {

    companion object {

        /**
         * Determines what the replacement for the quick fix must be.
         */
        fun replacement(it: Matcher) = if (it.group(1) != null) {
            "\\ll"
        }
        else if (it.group(2) != null) {
            "\\lll"
        }
        else if (it.group(3) != null) {
            "\\gg"
        }
        else {
            "\\ggg"
        }

        /**
         * Determines what range must be replaced.
         */
        fun replaceRange(it: Matcher): IntRange {
            for (i in 1..4) {
                if (it.group(i) != null) {
                    return it.groupRange(i)
                }
            }

            return IntRange.EMPTY
        }
    }

    override fun applyFixes(project: Project, descriptor: ProblemDescriptor, replacementRanges: List<IntRange>, replacements: List<String>, groups: List<List<String>>) {
        super.applyFixes(project, descriptor, replacementRanges, replacements, groups)

        // We overrided applyFixes instead of applyFix because all fixes need to be applied together, and only after that we insert any required package.
        val file = descriptor.psiElement.containingFile ?: return
        file.insertUsepackage(AMSSYMB)
    }
}