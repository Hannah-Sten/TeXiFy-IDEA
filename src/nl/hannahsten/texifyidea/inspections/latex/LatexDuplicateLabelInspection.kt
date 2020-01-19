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
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.findBibitemCommands
import nl.hannahsten.texifyidea.util.findLabelingCommandsSequence
import nl.hannahsten.texifyidea.util.parentOfType
import nl.hannahsten.texifyidea.util.requiredParameter
import java.lang.Integer.max
import java.util.*
import kotlin.collections.set

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

        // store the markers identified by the marked command to prevent to mark a command twice
        val markedCommands = mutableMapOf<PsiElement, ProblemDescriptor>()

        // list of defined commands
        val commands = TexifySettings.getInstance().labelCommands

        // Treat LaTeX and BibTeX separately because only the first parameter of \bibitem commands counts
        file.findLabelingCommandsSequence().forEach {
            // when label is defined in \newcommand ignore it, because there could be more than one with #1 as parameter
            val parent = it.parentOfType(LatexCommands::class)
            if (parent != null && parent.name == "\\newcommand") {
                return@forEach
            }

            val name = it.name ?: return@forEach

            val position = commands[name]?.position ?: return@forEach
            val label = it.requiredParameter(position - 1) ?: return@forEach
            markCommand(it, label, definedLabels, file, markedCommands, isOntheFly, manager)
        }

        // save all labels defined by bibitem commands
        val definedBibitems = mutableMapOf<String, LatexCommands>()

        file.findBibitemCommands().forEach {
            val label = it.requiredParameter(0) ?: return@forEach
            markCommand(it, label, definedBibitems, file, markedCommands, isOntheFly, manager)
        }

        // remove the identifier of the map and return as list
        return markedCommands.map { it.value }.toList()
    }

    /**
     * Mark a command as duplicate when applicable.
     */
    private fun markCommand(command: LatexCommands,
                    label: String,
                    definedLabels: MutableMap<String, LatexCommands>,
                    file: PsiFile,
                    markedCommands: MutableMap<PsiElement, ProblemDescriptor>,
                    isOntheFly: Boolean,
                    manager: InspectionManager) {

        if (definedLabels.contains(label)) {
            // We cannot mark commands which are not in the file we are currently inspecting
            // If the second one we found is not in the current file
            if (command.containingFile != file) {
                // Mark the old one
                val oldCommand = definedLabels[label] ?: return
                // To prevent marking a command twice, check if the command is already marked
                if (!markedCommands.containsKey(oldCommand)) {
                    markedCommands[oldCommand] = problemDescriptor(oldCommand, label, isOntheFly, manager)
                }
            }
            else {
                // The first one we found is in the current file, so mark it
                markedCommands[command] = problemDescriptor(command, label, isOntheFly, manager)
            }
        }
        // store the new label with defining command
        else {
            definedLabels[label] = command
        }
    }

    /**
     * calculates the offset between commandToken and the parameter defining the label
     */
    private fun skippedParametersLength(parameters : List<LatexParameter>, searched : String) : Int {
        val parameterStrings = parameters.map { latexParameter ->
            latexParameter.text
        }
        val toIgnore = parameterStrings.indexOf("{$searched}")
        return parameterStrings.subList(0, max(0, toIgnore)).map { it.length }
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
