package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.inspections.TexifyRegexInspection
import nl.rubensten.texifyidea.util.document
import org.intellij.lang.annotations.Language
import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class LatexEnDashInspection : TexifyRegexInspection(
        inspectionDisplayName = "En dash in number ranges",
        inspectionShortName = "EnDash",
        errorMessage = { "En dash expected" },
        pattern = Pattern.compile("(?<![0-9\\-])([0-9]+)\\s*[\\- ]+\\s*([0-9]+)(?=[^0-9\\-])")!!,
        quickFixName = { "Convert to en dash" },
        cancelIf = { matcher, _ -> CORRECT_EN_DASH.matcher(matcher.group()).matches() },
        groupFetcher = { listOf(it.group(1), it.group(2)) }
) {

    companion object {

        /**
         * This is the only correct way of using en dashes.
         */
        @Language("RegExp")
        val CORRECT_EN_DASH = Pattern.compile("[0-9]+--[0-9]+")!!
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor, replacementRange: IntRange, replacement: String, groups: List<String>) {
        val file = descriptor.psiElement as PsiFile
        val document = file.document() ?: return
        val start = groups[0]
        val end = groups[1]

        document.replaceString(replacementRange.start, replacementRange.endInclusive, "$start--$end")
    }
}