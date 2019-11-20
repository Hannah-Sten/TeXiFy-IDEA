package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInsight.daemon.quickFix.CreateFileFix
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LatexCommand
import nl.hannahsten.texifyidea.lang.RequiredArgument
import nl.hannahsten.texifyidea.lang.RequiredFileArgument
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.ui.CreateFileDialog
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.createFile
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.findRootFile
import java.io.File
import java.util.*

/**
 * @author Hannah Schellekens
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

            for (i in arguments.indices) {
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

                // check if the given name is reachable from the root file
                var relative = containingDirectory.findFile(fileName, extensions)

                // If not, check if it is reachable from any content root which will be included when using MiKTeX
                if (LatexDistribution.isMiktex) {
                    for (moduleRoot in ProjectRootManager.getInstance(file.project).contentSourceRoots) {
                        if (relative != null) {
                            break
                        }
                        relative = moduleRoot.findFile(fileName, extensions)
                    }
                }

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
                        InspectionFix()
                ))
            }
        }

        return descriptors
    }

    /**
     * Create a new file.
     */
    class InspectionFix : LocalQuickFix {

        override fun getFamilyName() = "Create file"

        override fun startInWriteAction() = false

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val cmd = descriptor.psiElement as LatexParameter
            val file = cmd.containingFile
            val root = file.findRootFile().containingDirectory.virtualFile.canonicalPath ?: return
            val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return

            // Create new files
            // Remove the braces of the LaTeX command before creating a filename of it
            val fileName = cmd.text.removeAll("{", "}")
                    .formatAsFileName()

            // Display a dialog to ask for the location and name of the new file.
            val filePath = CreateFileDialog(file.containingDirectory.virtualFile.canonicalPath, fileName.formatAsFileName())
                    .newFileFullPath ?: return

            runWriteAction {
                val createdFile = createFile("$filePath.tex", "")

                // Update LaTeX command parameter with chosen filename
                val fileNameRelativeToRoot = createdFile.absolutePath
                        .replace(File.separator, "/")
                        .replace("$root/", "")
                document.replaceString(cmd.textOffset + 1, cmd.endOffset() - 1, fileNameRelativeToRoot)
            }
        }
    }
}
