package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.webSymbols.references.WebSymbolReferenceProvider.Companion.startOffsetIn
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexRequiredParam
import nl.hannahsten.texifyidea.util.containsAny
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.firstChildOfType
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.requiredParameter

/**
 * @author Hannah Schellekens
 */
open class LatexSuspiciousSectionFormattingInspection : TexifyInspectionBase() {

    private val formatting = setOf("~", "\\\\")

    override val inspectionGroup = InsightGroup.LATEX

    override fun getDisplayName() = "Suspicious formatting in the required argument of a sectioning command"

    override val inspectionId = "SuspiciousFormatSection"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        return file.commandsInFile()
            .asSequence()
            .filter { it.name in CommandMagic.sectionMarkers }
            .filter { it.optionalParameterMap.isEmpty() }
            .filter { it.requiredParameter(0)?.containsAny(formatting) == true }
            .map { Pair(it, it.findTextRange() ?: TextRange(0, it.textLength)) }
            .map { (psiElement, textRange) -> manager.createProblemDescriptor(
                psiElement,
                textRange,
                "Suspicious formatting in ${psiElement.name}",
                ProblemHighlightType.WARNING,
                isOntheFly
            ) }
            .toList()
    }

    private fun LatexCommands.findTextRange(): TextRange? {
        val (startOffset, text) = requiredParameter(0)?.findAnyOf(formatting) ?: return null
        // Start offset of the required argument, plus 1 for the opening brace ({), plus the offset of the found string.
        val startInParent = firstChildOfType(LatexRequiredParam::class)?.startOffsetIn(this)?.plus(1)?.plus(startOffset) ?: return null
        return TextRange(startInParent, startInParent + text.length)
    }
}