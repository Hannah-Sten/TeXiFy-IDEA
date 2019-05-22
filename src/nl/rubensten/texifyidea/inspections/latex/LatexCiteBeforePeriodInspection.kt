package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import nl.rubensten.texifyidea.inspections.TexifyRegexInspection
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.*
import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class LatexCiteBeforePeriodInspection : TexifyRegexInspection(
        inspectionDisplayName = "Citations must be placed before interpunction",
        myInspectionId = "CiteBeforePeriod",
        errorMessage = { "\\cite is placed after interpunction" },
        pattern = Pattern.compile("([.,?!;:])~(\\\\cite)"),
        mathMode = false,
        replacement = { _, _ -> "" },
        replacementRange = { it.groupRange(1) },
        highlightRange = { it.groupRange(2).toTextRange() },
        quickFixName = { "Move interpunction to the back of \\cite" },
        groupFetcher = { listOf(it.group(1)) }
) {

    override fun applyFixes(project: Project, descriptor: ProblemDescriptor, replacementRanges: List<IntRange>, replacements: List<String>, groups: List<String>) {
        for (i in 0 until replacements.size) {
            val replacementRange = replacementRanges[i]

            val file = descriptor.psiElement.containingFile
            val document = file.document() ?: return
            val cite = file.findElementAt(replacementRange.endInclusive + 3)?.parentOfType(LatexCommands::class) ?: return
            val char = groups[0]

            if (cite.endOffset() >= document.textLength || document[cite.endOffset()] != char) {
                document.insertString(cite.endOffset(), char)
            }
        }

        super.applyFixes(project, descriptor, replacementRanges, replacements, groups)
    }
}