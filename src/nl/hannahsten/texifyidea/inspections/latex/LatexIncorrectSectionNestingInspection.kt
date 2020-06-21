package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.files.openedEditor
import nl.hannahsten.texifyidea.util.lineIndentation
import nl.hannahsten.texifyidea.util.replaceString

/**
 * @author Johannes Berger
 */
open class LatexIncorrectSectionNestingInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "IncorrectSectionNesting"

    override fun getDisplayName() = "Incorrect nesting"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val sectionCommands = LatexCommandsIndex.getCommandsByNames(file, "\\section", "\\subsection", "\\subsubsection", "\\paragraph", "\\subparagraph").sortedBy { it.textOffset }
        sectionCommands.forEachIndexed { index, command ->
            if (startsWithSubCommand(command, index) ||
                    subsubsectionAfterSection(command, sectionCommands, index) ||
                    subParagraphWithoutParagraph(command, sectionCommands, index)) {

                descriptors.add(manager.createProblemDescriptor(command,
                        "Incorrect nesting",
                        arrayOf(InsertParentCommandFix(), ChangeToParentCommandFix()),
                        ProblemHighlightType.WEAK_WARNING,
                        isOntheFly,
                        false)
                )
            }
        }
        return descriptors
    }

    private fun startsWithSubCommand(command: LatexCommands, index: Int): Boolean {
        val level = command.level()
        return ((level == 2 || level == 3 || level == 5) && index == 0)
    }

    private fun subsubsectionAfterSection(command: LatexCommands, sectionCommands: List<LatexCommands>, index: Int) =
            command.level() == 3 && sectionCommands[index - 1].level() == 1

    private fun subParagraphWithoutParagraph(command: LatexCommands, sectionCommands: List<LatexCommands>, index: Int) =
            (command.level() == 5 && sectionCommands[index - 1].level() < 4)

    private fun LatexCommands.level(): Int {
        return Magic.Command.labeledLevels
                .filterKeys { it.command == this.commandToken.text.removePrefix("\\") }
                .map { it.value }
                .firstOrNull() ?: error("Unexpected command")
    }

    private class InsertParentCommandFix : LocalQuickFix {

        override fun getFamilyName() = "Insert missing parent command"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val document = command.containingFile.document() ?: return
            val offset = command.textOffset
            val lineNumber = document.getLineNumber(offset)
            val newParentCommand = command.commandToken.text.replaceFirst("sub", "")
            val replacement = "$newParentCommand{}\n${document.lineIndentation(lineNumber)}"
            val caret = command.containingFile.openedEditor()?.caretModel
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
