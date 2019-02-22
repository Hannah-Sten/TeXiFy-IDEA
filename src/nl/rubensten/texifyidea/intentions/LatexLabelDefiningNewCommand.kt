package nl.rubensten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.settings.LabelingCommandInformation
import nl.rubensten.texifyidea.settings.TexifySettings
import nl.rubensten.texifyidea.settings.labeldefiningcommands.EditLabelDefiningCommand
import nl.rubensten.texifyidea.util.*

open class LatexLabelDefiningNewCommand : TexifyIntentionBase("Add label defining command to list") {
    private val settings = TexifySettings.getInstance()

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isLatexFile()) {
            return false
        }

        // check if selected part is \label
        val element = file.findElementAt(editor.caretModel.offset) ?: return false
        var selected = element as? LatexCommands ?: element.parentOfType(LatexCommands::class) ?: return false

        // get parent element
        val parentElement = selected.parent
        val parent = parentElement as? LatexCommands
                ?: parentElement.parentOfType(LatexCommands::class)

        // when element is \label and parent is \newcommand check if command is already in list
        if (selected.name == "\\label" && parent?.name == "\\newcommand") {
            // get name of the defined command
            val cmdName = parent.requiredParameter(0) ?: return false
            // check if there is already an entry wih this name
            return !settings.labelCommands.containsKey(cmdName)
        }

        // check if the parent of the current position is \newcommand, if true, set the selected command to the parent
        // command
        if (parent?.name == "\\newcommand") {
            selected = parent
        }

        // when command is \newcommand, check if it contains a \label
        if (selected.name == "\\newcommand") {
            val children = selected.childrenOfType(LatexCommands::class)
            if (children.none { it.name == "\\label" }) {
                return false
            }
            val cmdName = selected.requiredParameter(0) ?: return false
            return !settings.labelCommands.containsKey(cmdName)
        }

        return false
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null || !file.isLatexFile()) {
            return
        }

        // get current label
        val element = file.findElementAt(editor.caretModel.offset) ?: return
        val selected = element as? LatexCommands ?: element.parentOfType(LatexCommands::class) ?: return
        if (selected.name != "\\label") {
            return
        }

        // get parent command
        val parent = selected.parentOfType(LatexCommands::class) ?: return
        if (parent.name != "\\newcommand") {
            return
        }

        // get position of label command in children list
        val childCommands = parent.childrenOfType(LatexCommands::class)
        val firstLabel = childCommands.indexOfFirst { it.name == "\\label" }
        val childCommandBeforeLabel = childCommands.take(firstLabel)

        // get the name of the command
        val commandName = parent.requiredParameter(0) ?: return
        // get the position of the label in new command, identified by the argument number of the first required parameter
        // of label command
        val position = selected.requiredParameter(0)?.replace("#", "")
                ?.toIntOrNull() ?: 1
        // check if any command before the label command increases an counter which could be labeled to set the checkbox
        // to a possible correct status
        val labelAnyCommand = childCommandBeforeLabel.none { it.name in Magic.Command.increasesCounter }

        // initialize the dialog with computed values
        val newCommand = EditLabelDefiningCommand(commandName, position, labelAnyCommand)
        if (newCommand.showAndGet()) {
            // save the computed values
            settings.addCommand(LabelingCommandInformation(newCommand.getCommandName(), newCommand.getCommandPosition(),
                    newCommand.getLabelAnyPrevCommand()))
        }
    }

}