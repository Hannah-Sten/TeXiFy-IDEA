package nl.hannahsten.texifyidea.inspections.bibtex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.findAtLeast
import nl.hannahsten.texifyidea.util.includedPackages

/**
 * @author Hannah Schellekens
 */
open class BibtexDuplicateBibliographystyleInspection : TexifyInspectionBase() {

    // Manual override to match short name in plugin.xml
    override fun getShortName() = InsightGroup.BIBTEX.prefix + inspectionId

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "DuplicateBibliographystyle"

    override fun getDisplayName() = "Duplicate bibliography style commands"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {

        // Chapterbib allows multiple bibliographies
        if (file.includedPackages().any { it == "chapterbib" }) {
            return mutableListOf()
        }

        val descriptors = descriptorList()

        // Check if a bibliography is present.
        val commands = file.commandsInFileSet()
        commands.find { it.name == "\\bibliography" } ?: return descriptors

        if (commands.findAtLeast(2) { it.name == "\\bibliographystyle" }) {
            file.commandsInFile().asSequence()
                .filter { it.name == "\\bibliographystyle" }
                .forEach {
                    descriptors.add(
                        manager.createProblemDescriptor(
                            it,
                            TextRange(0, it.commandToken.textLength),
                            "\\bibliographystyle is already used elsewhere",
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOntheFly,
                            RemoveOtherCommandsFix
                        )
                    )
                }
        }

        return descriptors
    }

    /**
     * @author Hannah Schellekens
     */
    object RemoveOtherCommandsFix : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Remove other \\bibliographystyle commands"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val file = command.containingFile

            file.commandsInFileSet().asSequence()
                .filter { it.name == "\\bibliographystyle" && it != command }
                .forEach {
                    it.delete()
                }
        }
    }
}