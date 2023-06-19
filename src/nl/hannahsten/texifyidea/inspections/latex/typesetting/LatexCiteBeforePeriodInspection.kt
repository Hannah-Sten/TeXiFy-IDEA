package nl.hannahsten.texifyidea.inspections.latex.typesetting

import com.intellij.codeInspection.ProblemDescriptor
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.parser.endOffset
import nl.hannahsten.texifyidea.util.parser.parentOfType
import java.util.regex.Pattern
import kotlin.math.max

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
    groupFetcher = { listOf(it.group(1)) },
    cancelIf = { matcher, psiFile ->
        // Let's assume that an abbreviation before a cite which is not directly before a cite does not appear within n characters before the cite
        val range = matcher.groupRange(0)
        val subString = psiFile.text.substring(max(range.first - 6, 0), range.last)
        PatternMagic.abbreviation.toRegex().find(subString)?.groups?.isNotEmpty() == true || PatternMagic.unRegexableAbbreviations.any { subString.contains(it) }
    }
) {

    override fun applyFix(descriptor: ProblemDescriptor, replacementRange: IntRange, replacement: String, groups: List<String>): Int {
        val file = descriptor.psiElement.containingFile
        val document = file.document() ?: return 0
        val cite = file.findElementAt(replacementRange.last + 3)?.parentOfType(LatexCommands::class) ?: return 0

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