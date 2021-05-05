package nl.hannahsten.texifyidea.action

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiDocumentManager
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.runCommand
import java.util.concurrent.TimeUnit

class ReformatWithLatexindent : AnAction("Reformat File with Latexindent.pl") {

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
        val process = GeneralCommandLine("latexindent.pl", "-w", file.name)
            .withWorkDirectory(file.containingDirectory.virtualFile.path)
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .createProcess()
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

//        runCommand("latexindent.pl", "-w", file.name, file.containingDirectory.virtualFile.path)
    }
}