package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.RequiredArgument
import nl.hannahsten.texifyidea.lang.commands.RequiredFileArgument
import nl.hannahsten.texifyidea.util.files.commandsInFile
import java.io.File

/**
 * @author Thomas
 */
class LatexAbsolutePathInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "AbsolutePath"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val commands = file.commandsInFile()

        // Loop through commands of file
        for (command in commands) {
            // There may be multiple commands with this name, just guess the first one
            val latexCommand = LatexCommand.lookup(command.name)?.firstOrNull() ?: continue

            // Arguments from the LatexCommand (so the command as hardcoded in e.g. LatexRegularCommand)
            val requiredArguments = latexCommand.arguments.mapNotNull { it as? RequiredArgument }

            // Loop through arguments
            for (i in command.requiredParametersText().indices) {
                // Find the corresponding requiredArgument
                val requiredArgument = if (i < requiredArguments.size) requiredArguments[i] else requiredArguments.lastOrNull() ?: continue

                // Check if the actual argument is a file argument or continue with the next argument
                val fileArgument = requiredArgument as? RequiredFileArgument ?: continue
                val offset = command.text.indexOf(command.requiredParametersText()[i])
                if (offset == -1) continue
                val range = TextRange(0, command.requiredParametersText()[i].length).shiftRight(offset)

                if (File(range.substring(command.text)).isAbsolute && !fileArgument.isAbsolutePathSupported) {
                    descriptors.add(
                        manager.createProblemDescriptor(
                            command,
                            range,
                            "No absolute path allowed here",
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOntheFly
                        )
                    )
                }
            }
        }

        return descriptors
    }
}