package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInsight.daemon.quickFix.CreateFileFix
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LatexCommand
import nl.hannahsten.texifyidea.lang.RequiredArgument
import nl.hannahsten.texifyidea.lang.RequiredFileArgument
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.psi.impl.LatexCommandsImpl
import nl.hannahsten.texifyidea.ui.CreateFileDialog
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.Magic.Command.illegalExtensions
import nl.hannahsten.texifyidea.util.Magic.Command.includeOnlyExtensions
import nl.hannahsten.texifyidea.util.files.commandsInFile
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

        val commands = file.commandsInFile()
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
                val fileNames = parameter.splitContent()

                // get root file of the document actual worked with
                val root = file.findRootFile()

                // get the virtual file of the root file
                val containingDirectory = root.containingDirectory.virtualFile

                for (filePath in fileNames) {
                    // check if the given name is reachable from the root file
                    var relative = containingDirectory.findFile(filePath, extensions)

                    // If not, check if it is reachable from any content root which will be included when using MiKTeX
                    if (LatexDistribution.isMiktex) {
                        for (moduleRoot in ProjectRootManager.getInstance(file.project).contentSourceRoots) {
                            if (relative != null) {
                                break
                            }
                            relative = moduleRoot.findFile(fileName, extensions)
                        }
                    }

                    if (relative != null) continue

                    val fixes = mutableListOf(CreateFileFix(false, filePath, root.containingDirectory))

                    // Create quick fixes for all extensions if none was supplied in the argument
                    if (extensions.none { filePath.endsWith(".$it") }) {
                        extensions.forEach {
                            fixes.add(0, CreateFileFix(false, "$filePath.$it", root.containingDirectory))
                        }
                    }

                    // Find extension
                    val extension = if (command.commandToken.text in includeOnlyExtensions.keys) {
                        includeOnlyExtensions[command.commandToken.text]?.toList()?.first() ?: "tex"
                    }
                    else {
                        "tex"
                    }

                    val parameterOffset = parameter.text.trimRange(1,1).indexOf(filePath)
                    descriptors.add(manager.createProblemDescriptor(
                            parameter,
                            TextRange(parameterOffset + 1, parameterOffset + filePath.length + 1),
                            "File '${filePath.appendExtension(extension)}' not found",
                            ProblemHighlightType.GENERIC_ERROR,
                            isOntheFly,
                            InspectionFix(filePath, extension)
                    ))
                }
            }
        }

        return descriptors
    }

    /**
     * Create a new file.
     */
    class InspectionFix(private val filePath: String, private val extension: String) : LocalQuickFix {

        override fun getFamilyName() = "Create file ${filePath.appendExtension(extension)}"

        override fun startInWriteAction() = false

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val cmd = descriptor.psiElement as LatexParameter
            val file = cmd.containingFile
            val root = file.findRootFile().containingDirectory.virtualFile.canonicalPath ?: return
            val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return

            // Display a dialog to ask for the location and name of the new file. // todo may be bib file
            val newFilePath = CreateFileDialog(file.containingDirectory.virtualFile.canonicalPath, filePath.formatAsFilePath())
                    .newFileFullPath ?: return

            val parameterIndex = cmd.text.indexOf(filePath)

            runWriteAction {
                val createdFile = createFile("$newFilePath.$extension", "")

                // Update LaTeX command parameter with chosen filename
                var fileNameRelativeToRoot = createdFile.absolutePath
                        .replace(File.separator, "/")
                        .replace("$root/", "")

                val command = (cmd.context as LatexCommandsImpl).commandToken.text
                if (command in illegalExtensions) {
                    illegalExtensions[command]?.forEach { fileNameRelativeToRoot = fileNameRelativeToRoot.replace(it, "") }
                }

                document.replaceString(cmd.textOffset + parameterIndex, cmd.textOffset + parameterIndex + filePath.length, fileNameRelativeToRoot)
            }
        }
    }
}
