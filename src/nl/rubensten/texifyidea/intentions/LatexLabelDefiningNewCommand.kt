package nl.rubensten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.settings.LabelingCommandInformation
import nl.rubensten.texifyidea.settings.TexifySettings
import nl.rubensten.texifyidea.settings.labeldefiningcommands.EditLabelDefiningCommand
import nl.rubensten.texifyidea.util.isLatexFile
import nl.rubensten.texifyidea.util.parentOfType
import nl.rubensten.texifyidea.util.requiredParameter

open class LatexLabelDefiningNewCommand : TexifyIntentionBase("Add label defining command to list") {
    private val settings = TexifySettings.getInstance()
    override fun startInWriteAction(): Boolean = true

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isLatexFile()) {
            return false
        }

        val element = file.findElementAt(editor.caretModel.offset) ?: return false
        val selected = element as? LatexCommands ?: element.parentOfType(LatexCommands::class) ?: return false
        if (selected.name != "\\label") {
            return false
        }

        val parentElement = selected.parent
        val parent = parentElement as? LatexCommands ?:
                    parentElement.parentOfType(LatexCommands::class) ?: return false

        if (parent.name != "\\newcommand") {
            return false
        }

        val parameter = parent.requiredParameter(0) ?: return false
        return !settings.labelCommands.containsKey(parameter)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null || !file.isLatexFile()) {
            return
        }

        val element = file.findElementAt(editor.caretModel.offset) ?: return
        val selected = element as? LatexCommands ?: element.parentOfType(LatexCommands::class) ?: return
        if (selected.name != "\\label") {
            return
        }

        val parent = selected.parentOfType(LatexCommands::class) ?: return
        if (parent.name != "\\newcommand") {
            return
        }

        val commandName = parent.requiredParameter(0) ?: return
        val position = selected.requiredParameter(0)?.replace("#", "")
                ?.toIntOrNull() ?: 1

        val newCommand = EditLabelDefiningCommand(commandName, position, false)
        if (newCommand.showAndGet()) {
            settings.addCommand(LabelingCommandInformation(newCommand.getCommandName(), newCommand.getCommandPosition(),
                    newCommand.getLabelAnyPrevCommand()))
        }
    }

}