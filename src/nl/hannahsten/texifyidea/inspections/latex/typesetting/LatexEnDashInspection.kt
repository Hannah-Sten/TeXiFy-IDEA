package nl.hannahsten.texifyidea.inspections.latex.typesetting

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.length
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.toTextRange
import java.util.regex.Pattern

/**
 * @author Hannah Schellekens
 */
open class LatexEnDashInspection : TexifyRegexInspection(
    inspectionDisplayName = "En dash in number ranges",
    inspectionId = "EnDash",
    errorMessage = { "En dash expected" },
    pattern = Pattern.compile("(?<![0-9\\-])\\s+(([0-9]+)\\s*[\\- ]+\\s*([0-9]+))(\\s+|.)(?=[^0-9\\-])")!!,
    quickFixName = { "Convert to en dash" },
    cancelIf = { matcher, _ -> PatternMagic.correctEnDash.matcher(matcher.group(1)).matches() },
    replacementRange = { it.groupRange(1) },
    highlightRange = { it.groupRange(1).toTextRange() },
    groupFetcher = { listOf(it.group(2), it.group(3)) }
) {

    override fun applyFix(descriptor: ProblemDescriptor, replacementRange: IntRange, replacement: String, groups: List<String>): Int {
        val file = descriptor.psiElement as PsiFile
        val document = file.document() ?: return 0
        val start = groups[0]
        val end = groups[1]

        val dashReplacement = "$start--$end"
        document.replaceString(replacementRange.first, replacementRange.last, dashReplacement)

        return dashReplacement.length - replacementRange.length
    }
}