package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.inspections.AbstractTexifyCommandBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.nameWithoutSlash
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.ui.CreateFileDialog
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.getFileExtension
import nl.hannahsten.texifyidea.util.files.writeToFileUndoable
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.lookupCommandN
import java.util.*

/**
 * @author Hannah Schellekens
 */
class LatexFileNotFoundInspection : AbstractTexifyCommandBasedInspection(
    inspectionId = "FileNotFound",
    skipChildrenInContext = setOf(
        LatexContexts.Comment, LatexContexts.InsideDefinition
    )
) {

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun isAvailableForFile(file: PsiFile): Boolean {
        return LatexProjectStructure.isProjectFilesetsAvailable(file.project) && super.isAvailableForFile(file)
    }

    override fun inspectCommand(command: LatexCommands, contexts: LContextSet, defBundle: DefinitionBundle, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        val referencesList = InputFileReference.getFileArgumentsReferences(command)
        for (reference in referencesList) {
            if (reference.refText.isNotEmpty() && reference.resolve() == null) {
                createQuickFixes(reference, defBundle, descriptors, manager, isOnTheFly)
            }
        }
    }

    private fun createQuickFixes(
        reference: InputFileReference, defBundle: DefinitionBundle,
        descriptors: MutableList<ProblemDescriptor>, manager: InspectionManager, isOntheFly: Boolean
    ) {
        val fileName = reference.refText
        val commandName = reference.element.nameWithoutSlash
        val extensions = defBundle.lookupCommandN(commandName)?.arguments?.flatMap {
            LatexContexts.asFileInputCtx(it.contextSignature)?.supportedExtensions ?: emptyList()
        } ?: emptyList()

        // CTAN packages are no targets of the InputFileReference, so we check them here and don't show a warning if a CTAN package is included
        if (extensions.contains("sty")) {
            val ctanPackages = PackageUtils.CTAN_PACKAGE_NAMES.map { it.lowercase(Locale.getDefault()) }
            if (reference.refText.lowercase(Locale.getDefault()) in ctanPackages) return
        }

        val fixes = mutableListOf<LocalQuickFix>()

        // Create quick fixes for all extensions
        extensions.forEach {
            fixes.add(CreateNewFileWithDialogQuickFix(fileName, it, reference.element.createSmartPointer(), reference.refText, reference.range))
        }

        // Find expected extension
        val extension = fileName.getFileExtension().ifEmpty {
            extensions.firstOrNull()
        } ?: "tex"

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
    class CreateNewFileWithDialogQuickFix(private val filePath: String, private val extension: String, private val elementPointer: SmartPsiElementPointer<LatexCommands>, private val key: String, private val range: TextRange) : LocalQuickFix {

        override fun getFamilyName() = "Create file ${filePath.appendExtension(extension).formatAsFilePath()}"

        override fun startInWriteAction() = false

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val cmd = descriptor.psiElement
            val element = elementPointer.element ?: return
            val file = cmd.containingFile ?: return
            val root = file.findRootFile().containingDirectory?.virtualFile?.canonicalPath ?: return

            // Display a dialog to ask for the location and name of the new file.
            // By default, all inclusion paths are relative to the main file
            val newFilePath = CreateFileDialog(root, filePath.replace("$root/", ""))
                .newFileFullPath ?: return

            runWriteAction {
                val expandedFilePath = expandCommandsOnce(newFilePath, file.project, file.virtualFile)
                var fileNameRelativeToRoot = writeToFileUndoable(project, expandedFilePath, "", root, extension)

                val command = (cmd as? LatexCommands)?.name
                if (command in CommandMagic.illegalExtensions) {
                    CommandMagic.illegalExtensions[command]?.forEach { fileNameRelativeToRoot = fileNameRelativeToRoot.replace(it, "") }
                }

                InputFileReference.handleElementRename(element, fileNameRelativeToRoot, false, key, range)
            }
        }
    }
}
