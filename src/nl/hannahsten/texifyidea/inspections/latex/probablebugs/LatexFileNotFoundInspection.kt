package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.ui.CreateFileDialog
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.createFile
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.getFileExtension
import nl.hannahsten.texifyidea.util.magic.CommandMagic
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
            // Don't resolve references in command definitions, as in \cite{#1} the #1 is not a reference
            if (command.parent.parentsOfType(LatexCommands::class).any { it.name in CommandMagic.commandDefinitionsAndRedefinitions }) {
                continue
            }

            val referencesList = command.references.filterIsInstance<InputFileReference>()
            for (reference in referencesList) {
                if (reference.resolve() == null) {
                    createQuickFixes(reference, descriptors, manager, isOntheFly)
                }
            }
        }

        return descriptors
    }

    private fun createQuickFixes(reference: InputFileReference, descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean) {
        val fileName = reference.key
        val extensions = reference.extensions

        // CTAN packages are no targets of the InputFileReference, so we check them here and don't show a warning if a CTAN package is included
        if (extensions.contains("sty")) {
            val ctanPackages = PackageUtils.CTAN_PACKAGE_NAMES.map { it.lowercase(Locale.getDefault()) }
            if (reference.key.lowercase(Locale.getDefault()) in ctanPackages) return
        }

        val fixes = mutableListOf<LocalQuickFix>()

        // Create quick fixes for all extensions
        extensions.forEach {
            fixes.add(CreateNewFileWithDialogQuickFix(fileName, it, reference))
        }

        // Find expected extension
        val extension = fileName.getFileExtension().ifEmpty {
            reference.defaultExtension
        }

        descriptors.add(
            manager.createProblemDescriptor(
                reference.element,
                reference.rangeInElement,
                "File '${fileName.appendExtension(extension)}' not found",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOntheFly,
                *(fixes.toTypedArray())
            )
        )
    }

    /**
     * Create a new file.
     *
     * @param filePath Path relative to the root file parent.
     */
    class CreateNewFileWithDialogQuickFix(private val filePath: String, private val extension: String, private val reference: InputFileReference) : LocalQuickFix {

        override fun getFamilyName() = "Create file ${filePath.appendExtension(extension)}"

        override fun startInWriteAction() = false

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val cmd = descriptor.psiElement
            val file = cmd.containingFile ?: return
            val root = file.findRootFile().containingDirectory?.virtualFile?.canonicalPath ?: return

            // Display a dialog to ask for the location and name of the new file.
            // By default, all inclusion paths are relative to the main file
            val newFilePath = CreateFileDialog(file.findRootFile().containingDirectory?.virtualFile?.canonicalPath, filePath.formatAsFilePath())
                .newFileFullPath ?: return

            runWriteAction {
                val expandedFilePath = expandCommandsOnce(newFilePath, file.project, file) ?: newFilePath
                createFile("$expandedFilePath.$extension", "")

                // Update LaTeX command parameter with chosen filename.
                // We can safely add the extension since illegal extensions are removed later.
                // We add the extension for consistency with the file name auto completion, which also adds the extension.
                var fileNameRelativeToRoot = "$newFilePath.$extension"
                    .replace(File.separator, "/")
                    .replace("$root/", "")

                val command = (cmd as? LatexCommands)?.name
                if (command in CommandMagic.illegalExtensions) {
                    CommandMagic.illegalExtensions[command]?.forEach { fileNameRelativeToRoot = fileNameRelativeToRoot.replace(it, "") }
                }

                reference.handleElementRename(fileNameRelativeToRoot, false)
            }
        }
    }
}
