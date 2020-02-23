package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.lang.RequiredFileArgument
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.files.commandsInFile
import java.io.File

/**
 * @author Lukas Heiligenbrunner
 */
class LatexAbsolutePathInspection : TexifyInspectionBase() {
    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "AbsolutePath"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val commands = file.commandsInFile()

        // Loop through commands of file
        for (command in commands) {
            // Only consider default commands with a file argument.
            val name = command.commandToken.text
            val cmd = LatexRegularCommand[name.substring(1)] ?: continue

            val args = cmd.first().getArgumentsOf(RequiredFileArgument::class)
            if (args.isEmpty()) continue

            val support = args.first().isAbsolutePathSupported

            command.parameterList.first { firstParam -> firstParam.requiredParam != null }.childrenOfType(LatexNormalText::class).forEach { path ->
                if (File(path.text).isAbsolute && !support) {
                    // when absolute path and is not supported throw error

                    descriptors.add(manager.createProblemDescriptor(
                            command,
                            TextRange(0, command.text.length),
                            "No absolute path allowed here",
                            ProblemHighlightType.GENERIC_ERROR,
                            isOntheFly
                    ))
                }
            }
        }

        return descriptors
    }
}