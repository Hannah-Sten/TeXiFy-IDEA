package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInsight.daemon.quickFix.CreateFileFix
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.lang.LatexCommand
import nl.rubensten.texifyidea.lang.RequiredArgument
import nl.rubensten.texifyidea.lang.RequiredFileArgument
import nl.rubensten.texifyidea.lang.magic.MagicCommentScope
import nl.rubensten.texifyidea.psi.LatexNormalText
import nl.rubensten.texifyidea.util.findFile
import nl.rubensten.texifyidea.util.findRootFile
import nl.rubensten.texifyidea.util.firstChildOfType
import java.util.*

/**
 * @author Ruben Schellekens
 */
open class LatexFileNotFoundInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "FileNotFound"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "File not found"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        val commands = LatexCommandsIndex.getItems(file)
        for (command in commands) {
            // Only consider default commands with a file argument.
            val default = LatexCommand.lookup(command.name) ?: continue

            // Remove optional arguments from list of commands
            val arguments = default.arguments.mapNotNull { it as? RequiredArgument }

            // Remove optional parameters from list of parameters
            val parameters = command.parameterList.filter { it.requiredParam != null }

            for (i in 0 until arguments.size) {
                // when there are more required arguments than actual present break the loop
                if (i >= parameters.size) {
                    break
                }

                // check if actual argument is a file argument or continue with next argument
                val fileArgument = arguments[i] as? RequiredFileArgument ?: continue
                val extensions = fileArgument.supportedExtensions
                val parameter = parameters[i]

                // get file name of the command or continue with next parameter
                val fileName = parameter.requiredParam?.firstChildOfType(LatexNormalText::class)?.text ?: continue

                // get root file of the document actual worked with
                val root = file.findRootFile()

                // get the virtual file of the root file
                val containingDirectory = root.containingDirectory.virtualFile

                // check if the given name is reachable form the root file
                val relative = containingDirectory.findFile(fileName, extensions)

                if (relative != null) {
                    continue
                }

                val fixes: MutableList<LocalQuickFix> = mutableListOf(
                        CreateFileFix(false, fileName, root.containingDirectory)
                )

                // Create quick fixes for all extensions if none was supplied in the argument
                if (extensions.none { fileName.endsWith(".$it") }) {
                    extensions.forEach {
                        fixes.add(0, CreateFileFix(false, "$fileName.$it", root.containingDirectory))
                    }
                }

                descriptors.add(manager.createProblemDescriptor(
                        parameter,
                        TextRange(1, parameter.textLength - 1),
                        "File not found",
                        ProblemHighlightType.GENERIC_ERROR,
                        isOntheFly,
                        *fixes.toTypedArray()
                ))
            }
        }

        return descriptors
    }
}
