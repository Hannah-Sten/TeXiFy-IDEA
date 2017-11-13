package nl.rubensten.texifyidea.inspections.bibtex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.LatexIncludesIndex
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.commandsInFileSet
import nl.rubensten.texifyidea.util.requiredParameter

/**
 * @author Sten Wessel
 */
open class BibtexDuplicateBibliographyInspection : TexifyInspectionBase() {

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getInspectionId() = "DuplicateBibliography"

    // Manual override to match short name in plugin.xml
    override fun getShortName() = InsightGroup.BIBTEX.prefix + inspectionId

    override fun getDisplayName() = "Same bibliography is included multiple times"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        LatexIncludesIndex.getItemsInFileSet(file)
            .filter { it.name == "\\bibliography" }
            .groupBy { it.requiredParameters.getOrNull(0) }
            .filter { it.key != null && it.value.size > 1 }
            .flatMap { it.value }
            .forEach {
                descriptors.add(manager.createProblemDescriptor(
                    it, TextRange(0, it.requiredParameter(0)?.length!!).shiftRight(it.commandToken.textLength + 1),
                    "Bibliography file is included multiple times", ProblemHighlightType.GENERIC_ERROR,
                    isOntheFly,
                    RemoveOtherCommandsFix(it.requiredParameter(0)!!)
                ))
            }

        return descriptors
    }

    /**
     * @author Sten Wessel
     */
    class RemoveOtherCommandsFix(private val bibName: String) : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Remove other includes of $bibName"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val file = command.containingFile

            file.commandsInFileSet()
                    .filter { it.name == "\\bibliography" && it.requiredParameter(0) == command.requiredParameter(0) && it != command }
                    .forEach {
                        it.delete()
                    }
        }
    }
}
