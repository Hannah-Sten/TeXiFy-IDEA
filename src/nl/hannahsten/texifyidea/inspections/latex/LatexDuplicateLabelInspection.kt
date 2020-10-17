package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.CommandManager
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.util.findBibitemCommands
import nl.hannahsten.texifyidea.util.findLabelingCommandsInFileSetAsSequence
import nl.hannahsten.texifyidea.util.parentOfType
import nl.hannahsten.texifyidea.util.requiredParameter
import java.lang.Integer.max
import java.util.*

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

        val duplicateLabels = getProblemDescriptors(file.findLabelingCommandsInFileSetAsSequence(), isOntheFly, manager, file) {
            val name = this.name ?: return@getProblemDescriptors null
            val position = CommandManager.labelAliasesInfo.getOrDefault(name, null)?.positions?.firstOrNull() ?: return@getProblemDescriptors null
            this.requiredParameter(position) ?: return@getProblemDescriptors null
        }

        val duplicateBibitems = getProblemDescriptors(file.findBibitemCommands(), isOntheFly, manager, file) {
            this.requiredParameter(0) ?: return@getProblemDescriptors null
        }

        return duplicateLabels + duplicateBibitems
    }

    /**
     * @param getLabel Get the label string given the command in which it is defined.
     */
    private fun getProblemDescriptors(commands: Sequence<LatexCommands>, isOntheFly: Boolean, manager: InspectionManager, file: PsiFile, getLabel: LatexCommands.() -> String?): List<ProblemDescriptor> {

        // Map labels to commands defining the label
        val allLabels = hashMapOf<String, MutableSet<LatexCommands>>()

        val result = mutableListOf<ProblemDescriptor>()

        // Treat LaTeX and BibTeX separately because only the first parameter of \bibitem commands counts
        // First find all duplicate labels
        commands.forEach { command ->
            // When the label is defined in \newcommand ignore it, because there could be more than one with #1 as parameter
            val parent = command.parentOfType(LatexCommands::class)
            if (parent != null && parent.name == "\\newcommand") {
                return@forEach
            }

            allLabels.getOrPut(command.getLabel() ?: return@forEach) { mutableSetOf() }.add(command)
        }

        val duplicates = allLabels.filter { it.value.size > 1 }

        for ((label, labelCommands) in duplicates) {
            // We can only mark labels in the current file
            val currentFileDuplicates = labelCommands.filter { command -> command.containingFile == file }

            for (command in currentFileDuplicates) {
                result.add(createProblemDescriptor(command, label, isOntheFly, manager))
            }
        }

        return result
    }

    /**
     * calculates the offset between commandToken and the parameter defining the label
     */
    private fun skippedParametersLength(parameters: List<LatexParameter>, searched: String): Int {
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
    private fun createProblemDescriptor(cmd: LatexCommands, label: String, isOntheFly: Boolean, manager: InspectionManager):
        ProblemDescriptor {
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
