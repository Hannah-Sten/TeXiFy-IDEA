package nl.hannahsten.texifyidea.action.reformat

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessNotCreatedException
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import java.util.concurrent.TimeUnit

/**
 * An action which performs a reformat of the currently open file using a certain external tool.
 *
 * @author Thomas
 */
abstract class ExternalReformatAction(title: String, val isValidFile: (file: PsiFile) -> Boolean) : AnAction(title) {

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
     * @param fileName Name of the file.
     */
    abstract fun getCommand(fileName: String): List<String>

    override fun actionPerformed(event: AnActionEvent) {
        val dataContext = event.dataContext
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return
        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return
        val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return
        if (!isValidFile(file)) return
        val command = getCommand(file.name)
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
}