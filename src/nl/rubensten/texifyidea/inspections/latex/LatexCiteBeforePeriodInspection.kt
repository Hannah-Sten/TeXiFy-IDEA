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

    override fun applyFix(project: Project, descriptor: ProblemDescriptor, replacementRange: IntRange, replacement: String, groups: List<String>): Int {

        val file = descriptor.psiElement.containingFile
        val document = file.document() ?: return 0
        val cite = file.findElementAt(replacementRange.endInclusive + 3)?.parentOfType(LatexCommands::class) ?: return 0

        // Find the interpunction character (the first regex group) in order to move it after the cite
        val char = groups[0]

        if (cite.endOffset() >= document.textLength || document[cite.endOffset()] != char) {
            document.insertString(cite.endOffset(), char)
        }

        super.applyFix(project, descriptor, replacementRange, replacement, groups)

        // The document length did not change, so the increase is 0
        return 0
    }
}