package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jetbrains.rd.util.EnumSet
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.lang.predefined.CommandNames
import nl.hannahsten.texifyidea.lang.predefined.EnvironmentNames
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsSettingsManager
import nl.hannahsten.texifyidea.ui.CreateFileDialog
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.writeToFileUndoable
import nl.hannahsten.texifyidea.util.parser.*

/**
 * @author Hannah Schellekens
 */
open class LatexTooLargeSectionInspection : TexifyInspectionBase() {

    object Util {

        /**
         * All commands that count as inspected sections in order of hierarchy.
         */
        val SECTION_NAMES = listOf(CommandNames.CHAPTER, CommandNames.SECTION)

        /**
         * Looks up the section command that comes after the given command.
         *
         * The next commands has the same or higher level as the given one,
         * meaning that a \section will stop only  on \section and higher.
         *
         * This was written to require that a chpter or section command be passed
         *
         * As previously written, this would just match the first and second matching sections, but now
         * it will search ahead to find the first equal or bigger section, or EOF, whichever comes first
         */
        fun findNextSection(command: LatexCommands): PsiElement? {
            // Scan all commands.
            var commands = command.containingFile.commandsInFile().toList()
            commands = commands.subList(commands.indexOf(command) + 1, commands.size)

            val indexOfCurrent = SECTION_NAMES.indexOf(command.name)

            for (j in commands.indices) {
                val next = commands[j]

                val indexOfNext = SECTION_NAMES.indexOf(next.name)
                if (indexOfNext in 0..indexOfCurrent) {
                    return commands[j]
                }
            }

            // If no command was found, find the end of the document.
            return command.containingFile.traverseReversed().filterIsInstance<LatexEndCommand>().firstOrNull {
                it.environmentName() == EnvironmentNames.DOCUMENT
            }
        }
    }

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "TooLargeSection"

    override val ignoredSuppressionScopes: Set<MagicCommentScope> = EnumSet.of(MagicCommentScope.GROUP)

    override fun getDisplayName() = "Too large sections"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val commands = file.traverseCommands()
            .filter { cmd -> Util.SECTION_NAMES.contains(cmd.name) }.toList()

        for (i in commands.indices) {
            if (!isTooLong(commands[i], Util.findNextSection(commands[i]))) {
                continue
            }

            if (isAlreadySplit(commands[i], commands)) {
                return descriptors
            }

            descriptors.add(
                manager.createProblemDescriptor(
                    commands[i],
                    "Section is long and may be moved to a separate file.",
                    InspectionFix(),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly
                )
            )
        }

        return descriptors
    }

    /**
     * Checks if the given file is already a split up section or chapter, with [command] being the only section/chapter
     * in this file. (If [command] is a \chapter, \section can still occur.)
     */
    private fun isAlreadySplit(command: LatexCommands, commands: Collection<LatexCommands>): Boolean {
        return commands.count { cmd -> cmd.name == command.name } <= 1
    }

    /**
     * Checks if the given command starts a section that is too long.
     *
     * @param command
     *         The section command to start checking from.
     * @param nextCommand
     *         The section command after the `command` one, or `null` when there is no such command.
     * @return `true` when the command starts a section that is too long.
     */
    private fun isTooLong(command: LatexCommands, nextCommand: PsiElement?): Boolean {
        if (!Util.SECTION_NAMES.contains(command.name)) {
            return false
        }

        val file = command.containingFile
        val startIndex = command.textOffset + command.textLength
        val endIndex = nextCommand?.textOffset ?: file.textLength

        val conventionSettings = TexifyConventionsSettingsManager.getInstance(command.project).getSettings()
        val maxSectionSize = conventionSettings.currentScheme.maxSectionSize
        return (endIndex - startIndex) >= maxSectionSize
    }

    /**
     * @author Hannah Schellekens
     */
    class InspectionFix : LocalQuickFix {

        companion object {

            /**
             * Finds the label command of the given command.
             */
            fun findLabel(cmd: LatexCommands): LatexCommands? {
                val nextSibling = cmd.firstParentOfType(LatexNoMathContent::class)
                    ?.nextSiblingIgnoreWhitespace()
                    ?.findFirstChildOfType(LatexCommands::class) ?: return null
                return if (nextSibling.name == CommandNames.LABEL) nextSibling else null
            }
        }

        override fun startInWriteAction() = false

        override fun getFamilyName() = "Move section to another file"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val cmd = descriptor.psiElement as LatexCommands
            val nextCmd = Util.findNextSection(cmd)
            val label = findLabel(cmd)
            val file = cmd.containingFile
            val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return

            val startIndex = label?.endOffset() ?: cmd.endOffset()
            val cmdIndent = document.lineIndentation(document.getLineNumber(nextCmd?.textOffset ?: 0))
            val endIndex = (nextCmd?.textOffset ?: document.textLength) - cmdIndent.length
            val text = document.getText(TextRange(startIndex, endIndex)).trimEnd().removeIndents()

            // Create new file.
            val fileNameBraces = if (cmd.parameterList.isNotEmpty()) cmd.parameterList[0].text else return

            // Remove the braces of the LaTeX command before creating a filename of it
            val fileName = fileNameBraces.removeAll("{", "}")
                .formatAsFileName()
            val root = file.findRootFile().containingDirectory?.virtualFile?.canonicalPath ?: return

            // Display a dialog to ask for the location and name of the new file.
            val filePath =
                CreateFileDialog(file.containingDirectory?.virtualFile?.canonicalPath, fileName.formatAsFileName())
                    .newFileFullPath ?: return

            runWriteAction {
                val fn = writeToFileUndoable(project, filePath, text, root)
                document.deleteString(startIndex, endIndex)
                LocalFileSystem.getInstance().refresh(true)
                val indent = cmd.findIndentation()
                document.insertString(startIndex, "\n$indent\\input{${fn.dropLast(4)}}\n\n")
            }
        }
    }
}