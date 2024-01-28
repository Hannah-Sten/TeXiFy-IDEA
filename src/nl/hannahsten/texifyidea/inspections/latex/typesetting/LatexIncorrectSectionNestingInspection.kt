package nl.hannahsten.texifyidea.inspections.latex.typesetting

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.files.openedTextEditor
import nl.hannahsten.texifyidea.util.lineIndentation
import nl.hannahsten.texifyidea.util.replaceString

/**
 * @author Johannes Berger
 */
open class LatexIncorrectSectionNestingInspection : TexifyInspectionBase() {

    private val commandToForbiddenPredecessors = mapOf(
        """\part""" to emptyList(),
        """\chapter""" to emptyList(),
        """\section""" to emptyList(),
        """\subsection""" to listOf("""\part""", """\chapter"""),
        """\subsubsection""" to listOf("""\part""", """\chapter""", """\section"""),
        """\paragraph""" to emptyList(),
        """\subparagraph""" to listOf("""\part""", """\chapter""", """\section""", """\subsection""", """\subsubsection""")
    )

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "IncorrectSectionNesting"

    override fun getDisplayName() = "Incorrect nesting"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        return LatexCommandsIndex.Util.getCommandsByNames(file, *sectioningCommands())
            .sortedBy { it.textOffset }
            .zipWithNext()
            .filter { (first, second) ->
                first.commandName() in (commandToForbiddenPredecessors[second.commandName()] ?: error("Unexpected command"))
            }
            .map {
                manager.createProblemDescriptor(
                    it.second,
                    "Incorrect nesting",
                    arrayOf(InsertParentCommandFix(), ChangeToParentCommandFix()),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly,
                    false
                )
            }
    }

    private fun sectioningCommands() = commandToForbiddenPredecessors.keys.toTypedArray()

    private fun LatexCommands.commandName(): String = this.commandToken.text

    private class InsertParentCommandFix : LocalQuickFix {

        override fun getFamilyName() = "Insert missing parent command"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val document = command.containingFile.document() ?: return
            val offset = command.textOffset
            val lineNumber = document.getLineNumber(offset)
            val newParentCommand = command.commandToken.text.replaceFirst("sub", "")
            val replacement = "$newParentCommand{}\n${document.lineIndentation(lineNumber)}"
            val caret = command.containingFile.openedTextEditor()?.caretModel
            document.insertString(offset, replacement)
            caret?.moveToOffset(offset + newParentCommand.length + 1)
        }
    }

    private class ChangeToParentCommandFix : LocalQuickFix {

        override fun getFamilyName() = "Change to parent command"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val document = command.containingFile.document() ?: return
            val range = command.commandToken.textRange
            val newParentCommand = command.commandToken.text.replaceFirst("sub", "")
            document.replaceString(range, newParentCommand)
        }
    }
}
