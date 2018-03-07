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
import nl.rubensten.texifyidea.lang.RequiredFileArgument
import nl.rubensten.texifyidea.psi.LatexNormalText
import nl.rubensten.texifyidea.util.findRelativeFile
import nl.rubensten.texifyidea.util.findRootFile
import nl.rubensten.texifyidea.util.firstChildOfType

/**
 * @author Ruben Schellekens
 */
open class LatexFileNotFoundInspection : TexifyInspectionBase() {

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getInspectionId() = "FileNotFound"

    override fun getDisplayName() = "File not found"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        val commands = LatexCommandsIndex.getItems(file)
        for (cmd in commands) {
            // Only consider default commands with a file argument.
            val default = LatexCommand.lookup(cmd.name) ?: continue
            val arguments = default.arguments
            val parameters = cmd.parameterList

            for (i in 0 until arguments.size) {
                if (i >= parameters.size) {
                    break
                }

                val fileArgument = arguments[i] as? RequiredFileArgument ?: continue
                val extensions = fileArgument.supportedExtensions
                val parameter = parameters[i]
                val fileName = parameter.requiredParam?.firstChildOfType(LatexNormalText::class)?.text ?: continue
                val root = file.findRootFile()
                val relative = root.findRelativeFile(fileName, extensions)

                if (relative != null) {
                    continue
                }

                val fixes: MutableList<LocalQuickFix> = mutableListOf(
                        CreateFileFix(false, fileName, root.containingDirectory)
                )

                // Create quick fixes for all extensions if none was supplied in the argument
                if (extensions.none { fileName.endsWith(".$it") }) {
                    extensions.forEach { fixes.add(0, CreateFileFix(false, "$fileName.$it", root.containingDirectory)) }
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
