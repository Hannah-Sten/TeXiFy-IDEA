package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexParameter
import nl.rubensten.texifyidea.settings.TexifySettings
import nl.rubensten.texifyidea.util.*

/**
 * @author Ruben Schellekens, Sten Wessel
 */
open class LatexDuplicateLabelInspection : TexifyInspectionBase() {

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getDisplayName() = "Duplicate labels"

    override fun getInspectionId(): String = "DuplicateLabel"

    /**
     * checks if any label is used more than once
     */
    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {

        // save all labels defined by label commands
        val definedLabels = mutableMapOf<String, LatexCommands>()

        // save all labels defined by bibitem commands
        val definedBibitems = mutableMapOf<String, LatexCommands>()

        // store the markers identified by the marked command to prevent to mark a command twice
        val markedCommands = mutableMapOf<PsiElement, ProblemDescriptor>()

        // list of defined commands
        val commands = TexifySettings.getInstance().labelCommands

        // nearly same code twice, one time for labeling commands and one time for \bibitem
        file.findLabelingCommandsSequence().forEach {
            val position = commands[it.name]?.position ?: return@forEach
            val label = it.requiredParameter(position - 1) ?: return@forEach
            // when the label is already in the list, mark both, the older and the actual command
            if (definedLabels.contains(label)) {
                markedCommands[it] = problemDescriptor(it, label, isOntheFly, manager)
                val oldCommand = definedLabels[label] ?: return@forEach
                // to prevent marking a command twice, check if the command is allready marked
                if (!markedCommands.containsKey(oldCommand)) {
                    markedCommands[oldCommand] = problemDescriptor(oldCommand, label, isOntheFly, manager)
                }
            }
            // store the new label with defining command
            else {
                definedLabels[label] = it
            }
        }
        file.findBibitemCommands().forEach {
            val label = it.requiredParameter(0) ?: return@forEach
            if (definedBibitems.contains(label)) {
                markedCommands[it] = problemDescriptor(it, label, isOntheFly, manager)
                val oldCommand = definedBibitems[label] ?: return@forEach
                if (!markedCommands.containsKey(oldCommand)) {
                    markedCommands[oldCommand] = problemDescriptor(oldCommand, label, isOntheFly, manager)
                }
            }
            else {
                definedBibitems[label] = it
            }
        }

        // remove the identifier of the map and return as list
        return markedCommands.map { it.value }.toList()
    }

    /**
     * calculates the offset between commandToken and the parameter defining the label
     */
    private fun skippedParametersLength(parameters : List<LatexParameter>, searched : String) : Int {
        val parameterStrings = parameters.map { latexParameter ->
            latexParameter.text
        }
        val toIgnore = parameterStrings.indexOf("{$searched}")
        return parameterStrings.subList(0, toIgnore).map { it.length }
                .sum()
    }

    /**
     * make the mapping from command etc. to ProblemDescriptor
     */
    private fun problemDescriptor(cmd : LatexCommands, label : String, isOntheFly: Boolean, manager: InspectionManager)
            : ProblemDescriptor {
        val offset = cmd.commandToken.textLength + skippedParametersLength(cmd.parameterList, label) + 1
        return manager.createProblemDescriptor(
                cmd,
                TextRange.from(offset, label.length),
                "Duplicate label '$label'",
                ProblemHighlightType.GENERIC_ERROR,
                isOntheFly
        )
    }
}
