package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiDocumentManager
import nl.hannahsten.texifyidea.util.files.isLatexFile

class ReformatWithLatexindent : AnAction("Reformat File With Latexindent.pl") {
    override fun actionPerformed(event: AnActionEvent) {
        // Possible improvement: make visible anyway in LaTeX project (but only enabled if cursor in file)

        val presentation = event.presentation
        val dataContext = event.dataContext
        val project = CommonDataKeys.PROJECT.getData(dataContext)
        val editor = CommonDataKeys.EDITOR.getData(dataContext)
        if (project == null || editor == null) {
            presentation.isEnabledAndVisible = false
            return
        }

        val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
        if (file == null || file.virtualFile == null || file.isLatexFile()) {
            presentation.isEnabledAndVisible = false
            return
        }
    }
}