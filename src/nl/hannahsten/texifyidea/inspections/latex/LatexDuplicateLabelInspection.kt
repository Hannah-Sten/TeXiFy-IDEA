package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.bibtexIdsInFileSet
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import java.util.*
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.settings.TexifySettings

/**
 * @author Hannah Schellekens, Sten Wessel
 */
open class LatexDuplicateLabelInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId: String = "DuplicateLabel"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Duplicate labels"

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
            // when label is defined in \newcommand ignore it, because there could be mor than one with #1 as parameter
            val parent = it.parentOfType(LatexCommands::class)
            if (parent != null && parent.name == "\\newcommand") {
                return@forEach
            }

            val name = it.name ?: return@forEach

            val position = commands[name]?.position ?: return@forEach
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
