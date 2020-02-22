package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInsight.daemon.quickFix.CreateFilePathFix
import com.intellij.codeInsight.daemon.quickFix.NewFileLocation
import com.intellij.codeInsight.daemon.quickFix.TargetDirectory
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.lang.RequiredFileArgument
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.psi.impl.LatexCommandsImpl
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.ui.CreateFileDialog
import nl.hannahsten.texifyidea.util.Magic.Command.illegalExtensions
import nl.hannahsten.texifyidea.util.appendExtension
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.createFile
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.getFileExtension
import nl.hannahsten.texifyidea.util.formatAsFilePath
import nl.hannahsten.texifyidea.util.getFileArgumentsReferences
import nl.hannahsten.texifyidea.util.runWriteAction
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

        // Get commands of this file.
        val commands = file.commandsInFile()

        // Loop through commands of file
        for (command in commands) {
            val referencesList = command.getFileArgumentsReferences()
            for (reference in referencesList) {
                if (reference.resolve() == null) {
                    createQuickFixes(file, reference, command, descriptors, manager, isOntheFly)
                }
            }
        }

        return descriptors
    }

    private fun createQuickFixes(file: PsiFile, reference: InputFileReference, command: LatexCommands, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean) {
        val root = file.findRootFile()
        val fileName = reference.key
        val extensions = reference.extensions

        val newFileLocation = NewFileLocation(listOf(TargetDirectory(root.containingDirectory)), fileName)
        val fixes = mutableListOf(CreateFilePathFix(file, newFileLocation))

        // Create quick fixes for all extensions if no correct one was supplied in the argument
        if (extensions.none { fileName.endsWith(".$it") }) {
            extensions.forEach {
                val fileLocation = NewFileLocation(listOf(TargetDirectory(root.containingDirectory)), "$fileName.$it")
                fixes.add(CreateFilePathFix(file, fileLocation))
            }
        }

        // Find expected extension
        var extension = fileName.getFileExtension()
        if (extension == "") {
            val name = command.commandToken.text
            LatexRegularCommand[name.substring(1)]?.apply {
                val args = this.first().getArgumentsOf(RequiredFileArgument::class)
                if (args.isNotEmpty()) extension = args.first().defaultExtension
            }
        }

        descriptors.add(manager.createProblemDescriptor(
                reference.element,
                reference.range,
                "File '${fileName.appendExtension(extension)}' not found",
                ProblemHighlightType.GENERIC_ERROR,
                isOntheFly,
                *(fixes.toTypedArray())
        ))
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
