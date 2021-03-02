package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.startOffset
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.CommandManager
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
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

        val duplicateLabels =
            getProblemDescriptors(file.findLatexLabelPsiElementsInFileSetAsSequence(), isOntheFly, manager, file) {
                when (this) {
                    is LatexCommands -> {
                        val name = this.name ?: return@getProblemDescriptors null
                        if (CommandMagic.labelAsParameter.contains(this.name)) {
                            return@getProblemDescriptors getParameterLabelDescriptor(this)
                        }
                        else {
                            val position =
                                CommandManager.labelAliasesInfo.getOrDefault(name, null)?.positions?.firstOrNull()
                                    ?: return@getProblemDescriptors null
                            return@getProblemDescriptors getCommandLabelDescriptor(this, position)
                        }
                    }
                    is LatexEnvironment -> {
                        if (EnvironmentMagic.labelAsParameter.contains(this.environmentName)) {
                            return@getProblemDescriptors getParameterLabelDescriptor(this)
                        }

                        return@getProblemDescriptors null
                    }
                    else -> return@getProblemDescriptors null
                }
            }

        val duplicateBibitems = getProblemDescriptors(file.findBibitemCommands(), isOntheFly, manager, file) {
            getCommandLabelDescriptor(this as LatexCommands, 0)
        }

        return duplicateLabels + duplicateBibitems
    }

    /**
     * Creates a LabelDescriptor for an environment that defines a label with an optional parameter
     */
    private fun getParameterLabelDescriptor(env: LatexEnvironment): LabelDescriptor? {
        val label =
            env.beginCommand.optionalParameterMap.entries.firstOrNull() { e -> e.key.toString() == "label" }?.value
                ?: return null
        val labelString = label.toString()
        return LabelDescriptor(env, labelString, TextRange.from(label.startOffset - env.startOffset, label.textLength))
    }

    /**
     * Creates a LabelDescriptor for a command that defines a label with an optional parameter
     */
    private fun getParameterLabelDescriptor(cmd: LatexCommands): LabelDescriptor? {
        val label =
            cmd.optionalParameterMap.entries.firstOrNull() { e -> e.key.toString() == "label" }?.value ?: return null
        val labelString = label.toString()
        return LabelDescriptor(cmd, labelString, TextRange.from(label.startOffset - cmd.startOffset, label.textLength))
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
     * Creates a LabelDescriptor for a command that defines a label with a required parameter at the specified position
     */
    private fun getCommandLabelDescriptor(cmd: LatexCommands, position: Int): LabelDescriptor? {
        val label = cmd.requiredParameter(position) ?: return null
        val offset = cmd.commandToken.textLength + skippedParametersLength(cmd.parameterList, label) + 1
        return LabelDescriptor(cmd, label, TextRange.from(offset, label.length))
    }

    data class LabelDescriptor(val element: PsiElement, val label: String, val textRange: TextRange)

    /**
     * @param getLabelDescriptor Get the label string given the command in which it is defined.
     */
    private fun getProblemDescriptors(
        commands: Sequence<PsiElement>, isOntheFly: Boolean, manager: InspectionManager, file: PsiFile,
        getLabelDescriptor: PsiElement.() -> LabelDescriptor?
    ): List<ProblemDescriptor> {

        // Map labels to commands defining the label
        val labelDescriptors = mutableListOf<LabelDescriptor>()

        val result = mutableListOf<ProblemDescriptor>()

        // Treat LaTeX and BibTeX separately because only the first parameter of \bibitem commands counts
        // First find all duplicate labels
        commands.forEach { command ->
            // When the label is defined in \newcommand ignore it, because there could be more than one with #1 as parameter
            val parent = command.parentOfType(LatexCommands::class)
            if (parent != null && parent.name == "\\newcommand") {
                return@forEach
            }

            val labelDescriptor = command.getLabelDescriptor() ?: return@forEach
            labelDescriptors.add(labelDescriptor)
        }

        val duplicates = labelDescriptors.groupBy { d -> d.label }.filter { g -> g.value.size > 1 }

        for (group in duplicates) {
            // We can only mark labels in the current file
            val currentFileDuplicates = group.value.filter { descriptor -> descriptor.element.containingFile == file }

            for (labelDescriptor in currentFileDuplicates) {
                result.add(createProblemDescriptor(labelDescriptor, isOntheFly, manager))
            }
        }

        return result
    }

    /**
     * make the mapping from command etc. to ProblemDescriptor
     */
    private fun createProblemDescriptor(desc: LabelDescriptor, isOntheFly: Boolean, manager: InspectionManager):
            ProblemDescriptor {
        return manager.createProblemDescriptor(
            desc.element,
            desc.textRange,
            "Duplicate label '${desc.label}'",
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            isOntheFly
        )
    }
}
