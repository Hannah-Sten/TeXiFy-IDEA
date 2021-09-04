package nl.hannahsten.texifyidea.action.reformat

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessNotCreatedException
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import nl.hannahsten.texifyidea.BibtexLanguage
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.runWriteCommandAction
import java.util.concurrent.TimeUnit

/**
 * An action which performs a reformat of the currently open file using a certain external tool.
 *
 * @author Thomas
 */
abstract class ExternalReformatAction(title: String, val isValidFile: (file: PsiFile) -> Boolean) : AnAction(title), DumbAware {

    override fun update(event: AnActionEvent) {
        // Possible improvement: make visible anyway in LaTeX project (but only enabled if cursor in file)

        val presentation = event.presentation
        val dataContext = event.dataContext
        val project = CommonDataKeys.PROJECT.getData(dataContext)
        val editor = CommonDataKeys.EDITOR.getData(dataContext)
        if (project == null || editor == null) {
            presentation.isEnabledAndVisible = false
            return
        }

        // Only show when in a valid file
        val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
        if (file == null || file.virtualFile == null || (!isValidFile(file))) {
            presentation.isEnabledAndVisible = false
            return
        }
    }

    /**
     * Command to run the formatter.
     * Working directory will be the file's parent.
     *
     * Ideally, you use the file content from the psi tree, because when someone edits the file and directly
     * reformats afterwards, the file is most probably not yet saved to disk, so we have to do that ourselves.
     *
     * @param file File to reformat.
     */
    abstract fun getCommand(file: PsiFile): List<String>

    override fun actionPerformed(event: AnActionEvent) {
        val dataContext = event.dataContext
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return
        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return
        val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return
        if (!isValidFile(file)) return
        val command = getCommand(file)
        val process = try {
            GeneralCommandLine(command)
                .withWorkDirectory(file.containingDirectory.virtualFile.path)
                .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
                .createProcess()
        }
        catch (e: ProcessNotCreatedException) {
            Notification("LaTeX", "Could not run ${command.first()}", "Please double check if ${command.first()} is installed correctly: ${e.message}", NotificationType.ERROR).notify(project)
            return
        }
        val output = if (process.waitFor(3, TimeUnit.SECONDS)) {
            process.inputStream.bufferedReader().readText().trim() + process.errorStream.bufferedReader().readText().trim()
        }
        else {
            process.destroy()
            process.waitFor()
            null
        }
        if (process.exitValue() != 0) {
            Notification("LaTeX", "${command.first()} failed", output ?: "Exit value ${process.exitValue()}", NotificationType.ERROR).notify(project)
        }
        else if (output?.isNotBlank() == true) {
            processOutput(output, file, project)
        }
    }

    /**
     * Do whatever you want with the output of the program.
     */
    open fun processOutput(output: String, file: PsiFile, project: Project) {}

    /**
     * Replace the file content with the given output.
     * This is the preferred way, because it has immediate user feedback.
     * If you just modify the file content in the background, only on re-focus will the user be asked if he wants to see the external changes made.
     */
    fun replaceLatexFileContent(output: String, file: PsiFile, project: Project) {
        val newFile = LatexPsiHelper(project).createFromText(output)
        runWriteCommandAction(project) {
            file.node.replaceChild(file.node.firstChildNode, newFile.node.firstChildNode)
        }
    }

    /**
     * See [replaceLatexFileContent].
     */
    fun replaceBibtexFileContent(output: String, file: PsiFile, project: Project) {
        val newFile = PsiFileFactory.getInstance(project).createFileFromText("DUMMY.bib", BibtexLanguage, output, false, true)
        runWriteCommandAction(project) {
            file.node.replaceAllChildrenToChildrenOf(newFile.node)
        }
    }
}