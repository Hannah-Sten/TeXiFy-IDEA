package nl.hannahsten.texifyidea.action.debug

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

class ShowContextAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        // get the active editor
        val editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR) ?: return
        // get the file from the editor
        val file = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE)
            ?: return
        // get the caret position
        val caret = editor.caretModel.currentCaret
        // get the offset of the caret
        val offset = caret.offset
        // get the element at the caret position
        val element = file.findElementAt(offset) ?: return
        // show the context of the element in a dialog
        val project = e.project ?: return

        val semanticLookup = LatexDefinitionService.getInstance(e.project!!).getFilesetBundlesMerged(file)
        val contexts = LatexPsiUtil.resolveContextUpward(element, semanticLookup)
        println("Contexts: $contexts")

        val cmdName = element.firstParentOfType<LatexCommands>()?.name
        if(cmdName != null) {
            val cmdDef = LatexDefinitionService.getInstance(project).resolveCommandDef(file.virtualFile, cmdName)
            println("Definition of [$cmdName]: ${cmdDef ?: "Not found"}")
        }
    }
}