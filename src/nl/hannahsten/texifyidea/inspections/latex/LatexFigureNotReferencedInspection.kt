package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import java.util.*

open class LatexFigureNotReferencedInspection : TexifyInspectionBase() {

    override val inspectionGroup: InsightGroup = InsightGroup.LATEX

    override val inspectionId: String = "FigureNotReferenced"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName(): String = "Figure Not Referenced"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val figureLabels = getFigureLabels(file)

        removeReferencedLabels(file, figureLabels)

        val descriptors = descriptorList()
        for (label in figureLabels.values) {
            descriptors.add(createDescriptor(manager, label, isOntheFly))
        }

        return descriptors
    }

    private fun removeReferencedLabels(file: PsiFile, figureLabels: MutableMap<String?, LatexCommands>) {
        val referenceCommands = file.project.getLabelReferenceCommands()
        for (command in file.commandsInFileSet()) {
            // Don't resolve references in command definitions
            if (command.parent.firstParentOfType(LatexCommands::class)?.name in CommandMagic.commandDefinitions ||
                referenceCommands.contains(command.name)
            ) {
                command.referencedLabelNames.forEach { figureLabels.remove(it) }
            }
        }
    }

    private fun createDescriptor(manager: InspectionManager, label: LatexCommands, isOntheFly: Boolean): ProblemDescriptor =
        manager.createProblemDescriptor(
            label,
            "Figure is not referenced",
            isOntheFly,
            emptyArray(),
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING
        )

    /**
     * Find all commands in the file that label a figure.
     */
    private fun getFigureLabels(file: PsiFile): MutableMap<String?, LatexCommands> =
        file.findLabelingCommandsInFileAsSequence()
            .filter(this::isFigureLabel)
            .associateBy(LatexCommands::labelName)
            .toMutableMap()

    private fun isFigureLabel(label: LatexCommands): Boolean =
        label.inDirectEnvironment(EnvironmentMagic.figures)
}

private val LatexCommands.labelName: String?
    get() = requiredParameter(0)

private val LatexCommands.referencedLabelNames: List<String>
    get() = requiredParameter(0)?.split(",") ?: emptyList()
