package nl.hannahsten.texifyidea.inspections.latex.typesetting

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.inspections.AbstractTexifyCommandBasedInspection
import nl.hannahsten.texifyidea.inspections.createDescriptor
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.nameWithoutSlash
import nl.hannahsten.texifyidea.psi.prevContextualSibling
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import kotlin.math.max

/**
 * @author Hannah Schellekens
 */
class LatexCiteBeforePeriodInspection : AbstractTexifyCommandBasedInspection(
    inspectionId = "CiteBeforePeriod",
    applicableContexts = setOf(LatexContexts.Text)
) {

    private val prevRegex = Regex("([.,?!;:])~$") // Interpunction followed by a non-breaking space

    private fun isAbbreviationBefore(prevText: String): Boolean {
        // Let's assume that an abbreviation before a cite which is not directly before a cite does not appear within n characters before the cite
        val text = prevText.substring(max(0, prevText.length - 8), prevText.length)
        return PatternMagic.abbreviation.toRegex().find(text)?.groups?.isNotEmpty() == true || PatternMagic.unRegexableAbbreviations.any { text.contains(it) }
    }

    override fun inspectCommand(
        command: LatexCommands, contexts: LContextSet,
        defBundle: DefinitionBundle, file: PsiFile,
        manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>
    ) {
        if (command.nameWithoutSlash != "cite") return
        if (!isApplicableInContexts(contexts)) return
        val prev = command.prevContextualSibling() ?: return
        val prevText = prev.text
        val matcher = prevRegex.find(prevText) ?: return
        if (isAbbreviationBefore(prevText)) return
        val interpunction = matcher.groupValues[1]

        val descriptor = manager.createDescriptor(
            command,
            "\\cite is placed after interpunction",
            isOnTheFly = isOnTheFly,
            fix = LatexCiteBeforePeriodQuickFix(interpunction),
            rangeInElement = TextRange(0, 5),
        )
        descriptors.add(descriptor)
    }

    class LatexCiteBeforePeriodQuickFix(val interpunction: String) : LocalQuickFix {
        override fun getFamilyName(): @IntentionFamilyName String {
            return "Move interpunction to the back of \\cite"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val cite = descriptor.psiElement as? LatexCommands ?: return
            val file = cite.containingFile
            val document = file.document() ?: return
            val citeRange = cite.textRange
            document.replaceString(citeRange.startOffset - 2, citeRange.endOffset, "~${cite.text}$interpunction")
        }
    }
}