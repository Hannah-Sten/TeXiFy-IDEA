package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexPsiUtil
import nl.rubensten.texifyidea.util.TexifyUtil
import nl.rubensten.texifyidea.util.endOffset
import nl.rubensten.texifyidea.util.firstChildOfType
import org.intellij.lang.annotations.Language
import java.util.regex.Pattern
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * @author Ruben Schellekens
 */
open class TooLargeSectionInspection : TexifyInspectionBase() {

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
        private val TOO_LONG_LIMIT = 4000

        /**
         * Looks up the section command that comes after the given command.
         *
         * The next commands has the same or higher level as the given one,
         * meaning that a \section will stop only  on \section and higher.
         */
        fun findNextSection(command: LatexCommands): LatexCommands? {
            val commands = LatexCommandsIndex.getIndexCommands(command.containingFile).toList()

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

            return null
        }
    }

    override fun getDisplayName(): String {
        return "Too large sections"
    }

    override fun getShortName(): String {
        return "TooLargeSection"
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        val commands = LatexCommandsIndex.getIndexCommands(file)
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
                            "Section is long and may be moved to a seperate file.",
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
        val smallestIndex = commands.map { cmd -> SECTION_NAMES.indexOf(cmd.name) }.min() ?: return false

        // Just check if \section or \chapter occur only once.
        for (name in SECTION_NAMES) {
            if (SECTION_NAMES.indexOf(name) > smallestIndex) {
                continue
            }

            if (commands.filter { cmd -> cmd.name == name }.count() > 1) {
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
    private fun isTooLong(command: LatexCommands, nextCommand: LatexCommands?): Boolean {
        if (!SECTION_NAMES.contains(command.name)) {
            return false
        }

        val file = command.containingFile
        val startIndex = command.textOffset + command.textLength
        val endIndex = nextCommand?.textOffset ?: file.textLength

        return (endIndex - startIndex) >= TOO_LONG_LIMIT
    }

    private class InspectionFix : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Move section to another file"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val cmd = descriptor.psiElement as LatexCommands
            val nextCmd = findNextSection(cmd)
            val label = findLabel(cmd)
            val file = cmd.containingFile
            val document = PsiDocumentManager.getInstance(project).getDocument(file)

            val startIndex = label?.endOffset() ?: cmd.endOffset()
            val endIndex = nextCmd?.textOffset ?: document?.textLength ?: return
            val text = document?.getText(TextRange(startIndex, endIndex)) ?: return

            document.deleteString(startIndex, endIndex)

            val fileNameBraces = if (cmd.parameterList.size > 0) cmd.parameterList[0].text else return
            val fileName = fileNameBraces.replace("}", "").replace("{", "")
            val createdFile = TexifyUtil.createFile(file.containingDirectory.virtualFile.path + "/" + fileName + ".tex", text)
            LocalFileSystem.getInstance().refresh(true)

            val createdFileName = createdFile.name.subSequence(0, createdFile.name.length - 4)
            document.insertString(startIndex, "\n\\input{$createdFileName}\n\n")
        }

        /**
         * Finds the label command of the given command.
         */
        private fun findLabel(cmd: LatexCommands): LatexCommands? {
            val grandparent = cmd.parent.parent
            val sibling = LatexPsiUtil.getNextSiblingIgnoreWhitespace(grandparent) ?: return null
            val child = sibling.firstChildOfType(LatexCommands::class) ?: return null
            return if (child.name == "\\label") child else null
        }
    }
}
