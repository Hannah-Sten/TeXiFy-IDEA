package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.commands.LatexNewDefinitionCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.containsAny
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.GeneralMagic
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.matches
import nl.hannahsten.texifyidea.util.previousCommand
import nl.hannahsten.texifyidea.util.requiredParameter
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class LatexSuspiciousSectionFormattingInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override fun getDisplayName() = "Suspicious formatting in the required argument of a sectioning command"

    override val inspectionId = "SuspiciousFormatSection"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        return file.commandsInFile()
            .filter { it.name in CommandMagic.sectionMarkers }
            .filter { it.requiredParameter(0)?.containsAny(setOf("~", "\\\\")) == true }
            .map { manager.createProblemDescriptor(
                it,
                "Suspicious formatting in ${it.name}",
                GeneralMagic.noQuickFix,
                ProblemHighlightType.WARNING,
                isOntheFly
            ) }
    }
}