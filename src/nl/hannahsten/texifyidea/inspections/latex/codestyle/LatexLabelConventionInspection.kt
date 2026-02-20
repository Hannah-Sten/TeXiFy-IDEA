package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.rename.RenameProcessor
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.settings.conventions.LabelConventionType
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsSettingsManager
import nl.hannahsten.texifyidea.util.formatAsLabel
import nl.hannahsten.texifyidea.util.labels.LatexLabelUtil
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import nl.hannahsten.texifyidea.util.parser.traverseTyped
import java.util.*

/**
 * Check for label conventions, e.g. sec: in \section{A section}\label{sec:a-section}
 *
 * @author Hannah Schellekens
 */
class LatexLabelConventionInspection : TexifyInspectionBase() {

    private fun shouldBeBraced(labeledCommand: PsiElement): Boolean = when (labeledCommand) {
        is LatexCommands -> CommandMagic.labelAsParameter.contains(labeledCommand.name)
        is LatexEnvironment -> EnvironmentMagic.labelAsParameter.contains(labeledCommand.getEnvironmentName())
        else -> false
    }

    /**
     * Finds the expected prefix for the supplied label
     */
    private fun getLabelPrefix(labeledCommand: PsiElement): String? {
        val conventionSettings =
            TexifyConventionsSettingsManager.getInstance(labeledCommand.project).getSettings()

        return when (labeledCommand) {
            is LatexCommands -> {
                conventionSettings.getLabelConvention(
                    labeledCommand.name,
                    LabelConventionType.COMMAND
                )?.prefix
            }

            is LatexEnvironment -> {
                conventionSettings.getLabelConvention(
                    labeledCommand.getEnvironmentName(),
                    LabelConventionType.ENVIRONMENT
                )?.prefix
            }

            else -> null
        }
    }

    /**
     * Find the label of the environment. The method finds labels inside the environment content as well as labels
     * specified via an optional parameter
     * Similar to LabelExtraction#extractLabelElement, but we cannot use the index here
     *
     * @return the label name if any, null otherwise
     */
    private fun LatexEnvironment.getLabelParameterTextEnv(): LatexParameterText? {
        if (EnvironmentMagic.labelAsParameter.contains(this.getEnvironmentName())) {
            // See if we can find a label option
            return beginCommand.extractLabelParameterTextFromOptionalParameters()
        }
        // Not very clean. We don't really need the conventions here, but determine which environments *can* have a
        // label. However, if we didn't use the conventions, we would have to duplicate the information in
        // EnvironmentMagic
        // Find the nested label command in the environment content

        val content = this.environmentContent ?: return null
        // environment_content - no_math_content - commands
        val labelCommand = content.traverseCommands(2).firstOrNull {
            it.name in CommandMagic.labels
        } ?: return null
        // In fact, it is a simple \label command
        return labelCommand.findFirstChildTyped<LatexParameterText>()
    }

    private fun LatexCommandWithParams.extractLabelParameterTextFromOptionalParameters(): LatexParameterText? {
        forEachOptionalParameter { k, v ->
            if (k.text == "label" && v != null) {
                return v.findFirstChildTyped<LatexParameterText>()
            }
        }
        return null
    }

    private fun LatexCommands.getLabelParameterTextCommand(): LatexParameterText? {
        val name = this.name ?: return null
        if (CommandMagic.labelAsParameter.contains(name)) {
            return extractLabelParameterTextFromOptionalParameters()
        }
        if (name in CommandMagic.sectionNameToLevel) {
            traverseContextualSiblingsNext {
                if (it is LatexCommands) {
                    if (it.name in CommandMagic.labels) {
                        return it.findFirstChildTyped<LatexParameterText>()
                    }
                    if (it.name in CommandMagic.sectionNameToLevel) {
                        // end if we find another section
                        return null
                    }
                }
            }
        }
        return null
    }

    /**
     * Extracts the label name from the PsiElement given that the PsiElement represents a label.
     * This function should be quick to execute, because it may be called for all labels in the project very often.
     *
     *
     */
    private fun LatexComposite.extractLabelParameterText(): LatexParameterText? = when (this) {
        is LatexCommands -> getLabelParameterTextCommand()
        is LatexEnvironment -> getLabelParameterTextEnv()
        else -> null
    }

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "LabelConvention"

    override val ignoredSuppressionScopes = EnumSet.of(MagicCommentScope.COMMAND, MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Label conventions"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = mutableListOf<ProblemDescriptor>()
        checkLabels(file, manager, isOntheFly, descriptors)
        return descriptors
    }

    private fun checkLabels(
        file: PsiFile, manager: InspectionManager, isOntheFly: Boolean,
        descriptors: MutableList<ProblemDescriptor>
    ) {
        file.traverseTyped<LatexComposite>().forEach { element ->
            val labelParameterText = element.extractLabelParameterText() ?: return@forEach
            val labelName = labelParameterText.text
            val expectedPrefix = getLabelPrefix(element)
            if (!expectedPrefix.isNullOrBlank() && !labelName.startsWith("$expectedPrefix:")) {
                descriptors.add(
                    manager.createProblemDescriptor(
                        labelParameterText,
                        "Unconventional label prefix",
                        LabelPreFix(expectedPrefix, shouldBeBraced(element)),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                    )
                )
            }
        }
    }

    /**
     * @author Hannah Schellekens
     */
    private class LabelPreFix(
        val expectedPrefix: String,
        val shouldBeBraced: Boolean
    ) : LocalQuickFix {
        override fun startInWriteAction(): Boolean {
            // make the rename processor work
            return false
        }

        override fun getFamilyName() = "Fix label name"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val parameterText = descriptor.psiElement

            val baseFile = parameterText.containingFile
            val oldLabel = LatexLabelUtil.extractLabelTextIn(parameterText) ?: ""
            // Determine label name.
            val prefix: String = expectedPrefix
            val labelName = oldLabel.formatAsLabel()
            val createdLabelBase = if (labelName.contains(":")) {
                PatternMagic.labelPrefix.matcher(labelName).replaceAll("$prefix:")
            }
            else {
                "$prefix:$labelName"
            }
            var newLabel = LatexLabelUtil.getUniqueLabelName(createdLabelBase, baseFile)

            // let us add a braced label if it is not already braced
            if (shouldBeBraced) {
//                parameterText.firstParentOfType(LatexCommandWithParams::class)?.let { command ->
//                    LatexPsiHelper(project).setOptionalParameter(command, "label", "{$newLabel}")
//                    parameterText = command.extractLabelParameterTextFromOptionalParameters()
//                }
                newLabel = "{$newLabel}"
            }
            // use the renaming processor to rename the label, which will also update all references
            val processor = RenameProcessor(project, parameterText, newLabel, false, false)
            processor.run()
        }
    }
}