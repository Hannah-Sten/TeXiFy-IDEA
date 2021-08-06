package nl.hannahsten.texifyidea.action

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessNotCreatedException
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiDocumentManager
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.runWriteCommandAction
import java.util.concurrent.TimeUnit

/**
 * Run external tool 'latexindent.pl' to reformat the file.
 * This action is placed next to the standard Reformat action in the Code menu.
 *
 * @author Thomas
 */
class ReformatWithLatexindent : AnAction("Reformat File with Latexindent") {

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

        // Only show when in a LaTeX file
        val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
        if (file == null || file.virtualFile == null || !file.isLatexFile()) {
            presentation.isEnabledAndVisible = false
            return
        }
    }

    override fun actionPerformed(event: AnActionEvent) {
        val dataContext = event.dataContext
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return
        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return
        val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return
        if (!file.isLatexFile()) return
        val process = try {
            GeneralCommandLine("latexindent.pl", file.name)
                .withWorkDirectory(file.containingDirectory.virtualFile.path)
                .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
                .createProcess()
        }
        catch (e: ProcessNotCreatedException) {
            Notification("LaTeX", "Could not run latexindent.pl", "Please double check if latexindent.pl is installed correctly: ${e.message}", NotificationType.ERROR).notify(project)
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
            Notification("LaTeX", "Latexindent failed", output ?: "Exit value ${process.exitValue()}", NotificationType.ERROR).notify(project)
        }
        else if (output?.isNotBlank() == true) {
            // Assumes first child is LatexContent
            val newFile = LatexPsiHelper(project).createFromText(output)
            runWriteCommandAction(project) {
                file.node.replaceChild(file.node.firstChildNode, newFile.node.firstChildNode)
            }
        }
    }
}