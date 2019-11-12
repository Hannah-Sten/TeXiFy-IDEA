package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.ProblemDescriptor
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.document
import java.util.regex.Pattern

/**
 * @author Hannah Schellekens
 */
open class LatexCiteBeforePeriodInspection : TexifyRegexInspection(
        inspectionDisplayName = "Citations must be placed before interpunction",
        inspectionId = "CiteBeforePeriod",
        errorMessage = { "\\cite is placed after interpunction" },
        pattern = Pattern.compile("([.,?!;:])~(\\\\cite)"),
        mathMode = false,
        replacement = { _, _ -> "" },
        replacementRange = { it.groupRange(1) },
        highlightRange = { it.groupRange(2).toTextRange() },
        quickFixName = { "Move interpunction to the back of \\cite" },
        groupFetcher = { listOf(it.group(1)) }
) {

    override fun applyFix(descriptor: ProblemDescriptor, replacementRange: IntRange, replacement: String, groups: List<String>): Int {

        val file = descriptor.psiElement.containingFile
        val document = file.document() ?: return 0
        val cite = file.findElementAt(replacementRange.endInclusive + 3)?.parentOfType(LatexCommands::class) ?: return 0

        // Find the interpunction character (the first regex group) in order to move it after the cite
        val char = groups[0]

        if (cite.endOffset() >= document.textLength || document[cite.endOffset()] != char) {
            document.insertString(cite.endOffset(), char)
        }

        super.applyFix(descriptor, replacementRange, replacement, groups)

        // The document length did not change, so the increase is 0
        return 0
    }
}