package nl.hannahsten.texifyidea.inspections.latex

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
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEndCommand
import nl.hannahsten.texifyidea.psi.LatexPsiUtil
import nl.hannahsten.texifyidea.ui.CreateFileDialog
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.createFile
import nl.hannahsten.texifyidea.util.files.findRootFile
import org.intellij.lang.annotations.Language
import java.io.File
import java.util.*
import java.util.regex.Pattern

/**
 * @author Hannah Schellekens
 */
open class LatexTooLargeSectionInspection : TexifyInspectionBase() {

    companion object {

        @Language("RegExp")
        private val SECTION_COMMAND = Pattern.compile("\\\\(section|chapter)\\{[^{}]+}")

        /**
         * All commands that count as inspected sections in order of hierarchy.
         */
        private val SECTION_NAMES = listOf("\\chapter", "\\section")

        /**
         * The amount of characters it takes before a section is considered 'too long'.
         */
        private const val TOO_LONG_LIMIT = 4000

        /**
         * Looks up the section command that comes after the given command.
         *
         * The next commands has the same or higher level as the given one,
         * meaning that a \section will stop only  on \section and higher.
         */
        fun findNextSection(command: LatexCommands): PsiElement? {
            // Scan all commands.
            val commands = LatexCommandsIndex.getItems(command.containingFile).toList()

            for (i in 0 until commands.size) {
                val cmd = commands[i]

                val indexOfCurrent = SECTION_NAMES.indexOf(cmd.name)
                if (indexOfCurrent < 0) {
                    continue
                }

                if (cmd == command && i + 1 < commands.size) {
                    val next = commands[i + 1]

                    val indexOfNext = SECTION_NAMES.indexOf(next.name)
                    if (indexOfNext in 0..indexOfCurrent) {
                        return commands[i + 1]
                    }
                }
            }

            // If no command was found, find the end of the document.
            val children = command.containingFile.childrenOfType(LatexEndCommand::class)
            return if (children.isEmpty()) null else children.last()
        }
    }

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "TooLargeSection"

    override val ignoredSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Too large sections"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val commands = file.commandsInFile()
                .filter { cmd -> SECTION_NAMES.contains(cmd.name) }

        if (isAlreadySplit(commands)) {
            return descriptors
        }

        for (i in 0 until commands.size) {
            if (!isTooLong(commands[i], findNextSection(commands[i]))) {
                continue
            }

            descriptors.add(
                    manager.createProblemDescriptor(
                            commands[i],
                            "Section is long and may be moved to a separate file.",
                            InspectionFix(),
                            ProblemHighlightType.WEAK_WARNING,
                            isOntheFly
                    )
            )
        }

        return descriptors
    }

    /**
     * Checks if the given file is already a split up section.
     *
     * Not the best way perhaps, but it does the job.
     */
    private fun isAlreadySplit(commands: Collection<LatexCommands>): Boolean {
        val smallestIndex = commands.asSequence()
                .map { cmd -> SECTION_NAMES.indexOf(cmd.name) }
                .min() ?: return false

        // Just check if \section or \chapter occur only once.
        for (name in SECTION_NAMES) {
            if (SECTION_NAMES.indexOf(name) > smallestIndex) {
                continue
            }

            if (commands.asSequence().filter { cmd -> cmd.name == name }.count() > 1) {
                return false
            }
        }

        return true
    }

    /**
     * Checks if the given command starts a section that is too long.
     *
     * @param command
     *         The section command to start checking from.
     * @param nextCommand
     *         The section command after the `command` one, or `null` when there is no such command.
     * @return `true` when the command starts a section that is too long (see [TOO_LONG_LIMIT])
     */
    private fun isTooLong(command: LatexCommands, nextCommand: PsiElement?): Boolean {
        if (!SECTION_NAMES.contains(command.name)) {
            return false
        }

        val file = command.containingFile
        val startIndex = command.textOffset + command.textLength
        val endIndex = nextCommand?.textOffset ?: file.textLength

        return (endIndex - startIndex) >= TOO_LONG_LIMIT
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
                val grandparent = cmd.parent.parent
                val sibling = LatexPsiUtil.getNextSiblingIgnoreWhitespace(grandparent) ?: return null
                val child = sibling.firstChildOfType(LatexCommands::class) ?: return null
                return if (child.name == "\\label") child else null
            }
        }

        override fun startInWriteAction() = false

        override fun getFamilyName() = "Move section to another file"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val cmd = descriptor.psiElement as LatexCommands
            val nextCmd = findNextSection(cmd)
            val label = findLabel(cmd)
            val file = cmd.containingFile
            val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return

            val startIndex = label?.endOffset() ?: cmd.endOffset()
            val cmdIndent = document.lineIndentation(document.getLineNumber(nextCmd?.textOffset ?: 0))
            val endIndex = (nextCmd?.textOffset ?: document.textLength ?: return) - cmdIndent.length
            val text = document.getText(TextRange(startIndex, endIndex)).trimEnd().removeIndents()


            // Create new file.
            val fileNameBraces = if (cmd.parameterList.size > 0) cmd.parameterList[0].text else return

            // Remove the braces of the LaTeX command before creating a filename of it
            val fileName = fileNameBraces.removeAll("{", "}")
                    .formatAsFileName()
            val root = file.findRootFile().containingDirectory.virtualFile.canonicalPath ?: return

            // Display a dialog to ask for the location and name of the new file.
            val filePath = CreateFileDialog(file.containingDirectory.virtualFile.canonicalPath, fileName.formatAsFileName())
                    .newFileFullPath ?: return

            runWriteAction {
                val createdFile = createFile("$filePath.tex", text)
                document.deleteString(startIndex, endIndex)
                LocalFileSystem.getInstance().refresh(true)
                val fileNameRelativeToRoot = createdFile.absolutePath
                        .replace(File.separator, "/")
                        .replace(root, "")
                val indent = cmd.findIndentation()
                document.insertString(startIndex, "\n$indent\\input{${fileNameRelativeToRoot.dropLast(4)}}\n\n")
            }
        }
    }
}