package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import com.intellij.psi.search.searches.ReferencesSearch
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.SafeDeleteFix
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import nl.hannahsten.texifyidea.util.isFigureLabel
import nl.hannahsten.texifyidea.util.labels.findLabelingCommandsInFile
import nl.hannahsten.texifyidea.util.labels.getLabelReferenceCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import java.util.*

open class LatexFigureNotReferencedInspection : TexifyInspectionBase() {
    // What's the point of this inspection?
    // It is very likely that a figure is not referenced, but may be referenced in the future.

    override val inspectionGroup: InsightGroup = InsightGroup.LATEX

    override val inspectionId: String = "FigureNotReferenced"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName(): String = "Figure not referenced"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val figureLabels = file.traverseCommands().filter { it.isFigureLabel() }
        val descriptors = descriptorList()
        for (label in figureLabels) {
            // Remove labels that are referenced in the file
            val labelText = label.findFirstChildTyped<LatexParameterText>() ?: continue
            if(ReferencesSearch.search(labelText).none()) {
                descriptors.add(createDescriptor(manager, label, isOntheFly) ?: continue)
            }
        }

        return descriptors
    }

    private fun removeReferencedLabels(file: PsiFile, figureLabels: MutableMap<String?, LatexCommands>) {
        val referenceCommands = file.project.getLabelReferenceCommands()
        for (command in file.commandsInFileSet(useIndexCache = false)) {
            // Don't resolve references in command definitions
            if (command.parent?.firstParentOfType(LatexCommands::class)?.name in CommandMagic.commandDefinitionsAndRedefinitions ||
                referenceCommands.contains(command.name)
            ) {
                command.referencedLabelNames.forEach { figureLabels.remove(it) }
            }
        }
    }

    private fun createDescriptor(manager: InspectionManager, label: LatexCommands, isOntheFly: Boolean): ProblemDescriptor? =
        label.findFirstChildOfType(LatexParameterText::class)?.let {
            manager.createProblemDescriptor(
                it,
                "Figure is not referenced",
                RemoveFigureFix(it.createSmartPointer()),
                ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                isOntheFly
            )
        }

    /**
     * Find all commands in the file that label a figure.
     */
    private fun getFigureLabels(file: PsiFile): MutableMap<String?, LatexCommands> =
        file.findLabelingCommandsInFile()
            .filter(LatexCommands::isFigureLabel)
            .associateBy(LatexCommands::labelName)
            .toMutableMap()

    class RemoveFigureFix(label: SmartPsiElementPointer<LatexParameterText>) : SafeDeleteFix(label.element as PsiElement) {

        override fun getText(): String {
            return "Safe delete figure environment"
        }
    }
}

private val LatexCommands.labelName: String?
    get() = requiredParameterText(0)

private val LatexCommands.referencedLabelNames: List<String>
    get() = requiredParameterText(0)?.split(",") ?: emptyList()

fun dummy() = Unit
