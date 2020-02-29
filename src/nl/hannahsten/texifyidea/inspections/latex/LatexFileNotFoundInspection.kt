package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInsight.daemon.quickFix.CreateFilePathFix
import com.intellij.codeInsight.daemon.quickFix.NewFileLocation
import com.intellij.codeInsight.daemon.quickFix.TargetDirectory
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LatexCommand
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.lang.RequiredArgument
import nl.hannahsten.texifyidea.lang.RequiredFileArgument
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.psi.impl.LatexCommandsImpl
import nl.hannahsten.texifyidea.ui.CreateFileDialog
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.Magic.Command.illegalExtensions
import nl.hannahsten.texifyidea.util.files.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

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

        // Get all comands of project.
        val allCommands = file.commandsInFileSet()
        // Get commands of this file.
        val commands = file.commandsInFile()

        val graphPaths: ArrayList<String> = ArrayList()

        // Check if a graphicspath is defined
        val collection = allCommands.filter { it.name == "\\graphicspath" }

        // Is a graphicspath defined?
        if (collection.isNotEmpty()) {
            // Check if there is even an includegraphics in local commandset
            if (commands.any { it.name == "\\includegraphics" }) {
                val args = collection.last().parameterList.filter { it.requiredParam != null }
                val subArgs = args.first().childrenOfType(LatexNormalText::class)
                subArgs.forEach { graphPaths.add(it.text) }
            }
        }

        // Loop through commands of file
        for (command in commands) {
            // Only consider default commands with a file argument.
            val default = LatexCommand.lookup(command.name) ?: continue

            // Remove optional arguments from list of commands
            val arguments = default.first().arguments.mapNotNull { it as? RequiredArgument }

            // Remove optional parameters from list of parameters
            val parameters = command.parameterList.filter { it.requiredParam != null }

            // Loop through arguments
            for (i in arguments.indices) {
                // when there are more required arguments than actual present break the loop
                if (i >= parameters.size) {
                    break
                }

                // check if actual argument is a file argument or continue with next argument
                val fileArgument = arguments[i] as? RequiredFileArgument ?: continue
                val extensions = fileArgument.supportedExtensions
                val parameter = parameters[i]

                goThroughFileNames(file, parameter, command, graphPaths, extensions, manager, isOntheFly, descriptors)
            }
        }

        return descriptors
    }

    private fun goThroughFileNames(file: PsiFile, parameter: LatexParameter, command: LatexCommands, graphPaths: ArrayList<String>,
                                   extensions: Set<String>, manager: InspectionManager, isOntheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        // get file name of the command or continue with next parameter
        val fileNames = parameter.splitContent()

        // get root file of the document actual worked with
        val root = file.findRootFile()

        // get the virtual file of the root file
        val containingDirectory = root.containingDirectory?.virtualFile ?: return

        for (fileName in fileNames) {
            // Check if command is a includegraphics - next file if it exists
            if (command.name.equals("\\includegraphics")) {
                if (findGraphicsFile(containingDirectory, extensions, graphPaths, fileName)) continue
            }
            else {
                if (findGeneralFile(containingDirectory, file, extensions, fileName)) continue
            }

            val newFileLocation = NewFileLocation(listOf(TargetDirectory(root.containingDirectory)), fileName)
            val fixes = mutableListOf(CreateFilePathFix(file, newFileLocation))

            // Create quick fixes for all extensions if none was supplied in the argument
            if (extensions.none { fileName.endsWith(".$it") }) {
                extensions.forEach {
                    val fileLocation = NewFileLocation(listOf(TargetDirectory(root.containingDirectory)), "$fileName.$it")
                    fixes.add(CreateFilePathFix(file, fileLocation))
                }
            }

            // Find extension
            var extension = fileName.getFileExtension()
            if (extension == "") {
                val name = command.commandToken.text
                LatexRegularCommand[name.substring(1)]?.apply {
                    val args = this.first().getArgumentsOf(RequiredFileArgument::class)
                    if (args.isNotEmpty()) extension = args.first().defaultExtension
                }
            }

            val parameterOffset = parameter.text.trimRange(1, 1).indexOf(fileName)

            descriptors.add(manager.createProblemDescriptor(
                    parameter,
                    TextRange(parameterOffset + 1, parameterOffset + fileName.length + 1),
                    "File '${fileName.appendExtension(extension)}' not found",
                    ProblemHighlightType.GENERIC_ERROR,
                    isOntheFly,
                    InspectionFix(fileName, extension)
            ))
        }
    }

    private fun findGeneralFile(containingDir: VirtualFile, file: PsiFile, validExtenions: Set<String>, fileName: String): Boolean {
        if (File(fileName).isAbsolute) {
            val fs = LocalFileSystem.getInstance()

            if (fs.findFileByPath(fileName) != null) return true
            validExtenions.forEach {
                if (fs.findFileByPath("$fileName.$it") != null) return true
            }
        }
        else {
            // check if the given name is reachable from the given folder
            var relative = containingDir.findFile(fileName, validExtenions)

            // If not, check if it is reachable from any content root which will be included when using MiKTeX
            if (LatexDistribution.isMiktex) {
                for (moduleRoot in ProjectRootManager.getInstance(file.project).contentSourceRoots) {
                    if (relative != null) {
                        break
                    }
                    relative = moduleRoot.findFile(fileName, validExtenions)
                }
            }

            // If file was found continue with next file
            return (relative != null)
        }
        return false
    }

    private fun findGraphicsFile(containingDir: VirtualFile, validExtenions: Set<String>, searchPaths: ArrayList<String>, fileName: String): Boolean {
        val fs = LocalFileSystem.getInstance()
        if (File(fileName).isAbsolute) {
            // If file was found continue with next file
            if (fs.findFileByPath(fileName) != null) return true
            validExtenions.forEach {
                if (fs.findFileByPath("$fileName.$it") != null) return true
            }
        }
        else {
            searchPaths.forEach { searchPath ->
                // graphicspath can be absolute or relative
                if (File(searchPath).isAbsolute) {
                    if (fs.findFileByPath(searchPath + fileName) != null) return true
                    // search for supported extensions
                    validExtenions.forEach { extension ->
                        if (fs.findFileByPath("$searchPath$fileName.$extension") != null) return true
                    }
                }
                else {
                    // find relative file
                    if (containingDir.findFile(searchPath + fileName, validExtenions) != null) return true
                }
            }

            // check also the root folder
            return (containingDir.findFile(fileName, validExtenions) != null)
        }
        return false
    }

    /**
     * Create a new file.
     */
    class InspectionFix(private val filePath: String, private val extension: String) : LocalQuickFix {

        override fun getFamilyName() = "Create file ${filePath.appendExtension(extension)}"

        override fun startInWriteAction() = false

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val cmd = descriptor.psiElement as LatexParameter
            val file = cmd.containingFile ?: return
            val root = file.findRootFile().containingDirectory.virtualFile.canonicalPath ?: return
            val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return

            // Display a dialog to ask for the location and name of the new file.
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
