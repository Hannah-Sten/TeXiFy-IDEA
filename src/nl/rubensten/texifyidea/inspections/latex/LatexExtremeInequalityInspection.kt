package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.inspections.TexifyRegexInspection
import nl.rubensten.texifyidea.lang.Package
import nl.rubensten.texifyidea.util.insertUsepackage
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class LatexExtremeInequalityInspection : TexifyRegexInspection(
        inspectionDisplayName = "Use the matching amssymb symbol for inequalities",
        inspectionShortName = "ExtremeInequality",
        errorMessage = { "Use the amssymb symbol instead." },
        pattern = Pattern.compile("(<\\s*<(?!\\s*<))|(<\\s*<\\s*<)|(>\\s*>(?!\\s*>))|(>\\s*>\\s*>)"),
        mathMode = true,
        replacement = this::replacement,
        replacementRange = this::replaceRange,
        quickFixName = { "Insert amssymb symbol." }
) {

    companion object {

        /**
         * Determines what the replacement for the quick fix must be.
         */
        fun replacement(it: Matcher, file: PsiFile) = if (it.group(1) != null) {
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

    override fun applyFix(project: Project, descriptor: ProblemDescriptor, replacementRange: IntRange, replacement: String, groups: List<String>) {
        super.applyFix(project, descriptor, replacementRange, replacement, groups)

        val file = descriptor.psiElement.containingFile ?: return
        file.insertUsepackage(Package.AMSSYMB)
    }
}