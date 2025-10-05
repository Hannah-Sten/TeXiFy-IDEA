package nl.hannahsten.texifyidea.inspections.bibtex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.files.openedTextEditor
import nl.hannahsten.texifyidea.util.lineIndentationByOffset
import nl.hannahsten.texifyidea.util.parser.endOffset

/**
 * @author Hannah Schellekens
 */
open class BibtexMissingBibliographystyleInspection : TexifyInspectionBase() {

    // Manual override to match short name in plugin.xml
    override fun getShortName() = InsightGroup.BIBTEX.prefix + inspectionId

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "MissingBibliographystyle"

    override fun getDisplayName() = "Missing bibliography style"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        val bibCmd = file.traverseCommands().find {
            it.name == "\\bibliography"
        } ?: return descriptors
        val noStyle = NewCommandsIndex.getByNameInFileSet("\\bibliographystyle", file).isEmpty()
        if (noStyle) {
            descriptors.add(
                manager.createProblemDescriptor(
                    bibCmd,
                    TextRange(0, bibCmd.commandToken.textLength),
                    "No \\bibliographystyle defined",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly,
                    InsertStyleFix
                )
            )
        }

        return descriptors
    }

    /**
     * @author Hannah Schellekens
     */
    object InsertStyleFix : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Insert \\bibliographystyle"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val file = command.containingFile
            val document = file.document() ?: return
            val editor = file.openedTextEditor() ?: return

            val offset = command.endOffset()
            val indent = document.lineIndentationByOffset(offset)
            val string = "\n$indent\\bibliographystyle{}"
            document.insertString(offset, string)
            editor.caretModel.moveToOffset(offset + string.length - 1)
        }
    }
}
