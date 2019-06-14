package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.inspections.TexifyRegexInspection
import nl.rubensten.texifyidea.util.Magic
import nl.rubensten.texifyidea.util.document
import nl.rubensten.texifyidea.util.length
import nl.rubensten.texifyidea.util.toTextRange
import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class LatexEnDashInspection : TexifyRegexInspection(
        inspectionDisplayName = "En dash in number ranges",
        myInspectionId = "EnDash",
        errorMessage = { "En dash expected" },
        pattern = Pattern.compile("(?<![0-9\\-])\\s+(([0-9]+)\\s*[\\- ]+\\s*([0-9]+))(\\s+|.)(?=[^0-9\\-])")!!,
        quickFixName = { "Convert to en dash" },
        cancelIf = { matcher, _ -> Magic.Pattern.correctEnDash.matcher(matcher.group(1)).matches() },
        replacementRange = { it.groupRange(1) },
        highlightRange = { it.groupRange(1).toTextRange() },
        groupFetcher = { listOf(it.group(2), it.group(3)) }
) {

    override fun applyFix(project: Project, descriptor: ProblemDescriptor, replacementRange: IntRange, replacement: String, groups: List<String>): Int {
        val file = descriptor.psiElement as PsiFile
        val document = file.document() ?: return 0
        val start = groups[0]
        val end = groups[1]

        val dashReplacement = "$start--$end"
        document.replaceString(replacementRange.start, replacementRange.endInclusive, dashReplacement)

        return dashReplacement.length - replacementRange.length
    }
}