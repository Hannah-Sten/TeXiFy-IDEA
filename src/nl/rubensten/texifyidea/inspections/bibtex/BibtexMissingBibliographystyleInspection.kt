package nl.rubensten.texifyidea.inspections.bibtex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.*

/**
 * @author Ruben Schellekens
 */
open class BibtexMissingBibliographystyleInspection : TexifyInspectionBase() {

    // Manual override to match short name in plugin.xml
    override fun getShortName() = InsightGroup.BIBTEX.prefix + inspectionId

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getInspectionId() = "MissingBibliographystyle"

    override fun getDisplayName() = "Missing bibliography style"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        val commands = file.commandsInFileSet()
        val bibCmd = commands.find { it.containingFile == file && it.name == "\\bibliography" } ?: return descriptors
        if (commands.none { it.name == "\\bibliographystyle" }) {
            descriptors.add(manager.createProblemDescriptor(
                    bibCmd,
                    TextRange(0, bibCmd.commandToken.textLength),
                    "No \\bibliographystyle defined",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly,
                    InsertStyleFix
            ))
        }

        return descriptors
    }

    /**
     * @author Ruben Schellekens
     */
    object InsertStyleFix : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Insert \\bibliographystyle"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val file = command.containingFile
            val document = file.document() ?: return
            val editor = file.openedEditor() ?: return

            val offset = command.endOffset()
            val indent = document.lineIndentationByOffset(offset)
            val string = "\n$indent\\bibliographystyle{}"
            document.insertString(offset, string)
            editor.caretModel.moveToOffset(offset + string.length - 1)
        }
    }
}
