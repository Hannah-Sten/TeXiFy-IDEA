package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.settings.LabelingCommandInformation
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.settings.labeldefiningcommands.EditLabelDefiningCommand
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.isLatexFile

open class LatexLabelDefiningNewCommand : TexifyIntentionBase("Add label defining command to settings") {

    private val settings = TexifySettings.getInstance()

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isLatexFile()) {
            return false
        }

        // check if selected part is \label
        val element = file.findElementAt(editor.caretModel.offset) ?: return false
        var selected = element as? LatexCommands
                ?: element.parentOfType(LatexCommands::class)
                ?: return false

        // get parent element
        val parent = selected.parent as? LatexCommands
                ?: selected.parent.parentOfType(LatexCommands::class)

        // when element is \label and parent is \newcommand check if command is already in list
        if (settings.labelCommands.containsKey(selected.name) && parent?.name == "\\newcommand") {
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
            if (children.none { settings.labelCommands.containsKey(it.name) }) {
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

        // check if selected part is \label
        val element = file.findElementAt(editor.caretModel.offset) ?: return
        val selected = element as? LatexCommands ?: element.parentOfType(LatexCommands::class) ?: return

        // get parent element
        val parent = selected.parent as? LatexCommands
                ?: selected.parent.parentOfType(LatexCommands::class)

        val label: LatexCommands
        val newCommand: LatexCommands

        // map correct values to label and newCommand
        if (settings.labelCommands.containsKey(selected.name) && parent?.name == "\\newcommand") {
            label = selected
            newCommand = parent
        }
        else if (selected.name == "\\newcommand") {
            label = selected.childrenOfType(LatexCommands::class)
                    .firstOrNull { settings.labelCommands.containsKey(it.name) } ?: return
            newCommand = selected
        }
        else if (parent?.name == "\\newcommand") {
            label = parent.childrenOfType(LatexCommands::class)
                    .firstOrNull { settings.labelCommands.containsKey(it.name) } ?: return
            newCommand = parent
        }
        else {
            return
        }

        // get the name of the command
        val commandName = newCommand.requiredParameter(0) ?: return
        // get the position of the label in new command, identified by the argument number of the first required parameter
        // of label command
        val position = label
                .requiredParameter((settings.labelCommands[label.name]?.position ?: 1) - 1)
                ?.replace("#", "")
                ?.toIntOrNull() ?: 1
        // check if any command before the label command increases an counter which could be labeled to set the checkbox
        // to a possible correct status
        val labelAnyCommand = newCommand
                .childrenOfType(LatexCommands::class)
                .none { it.name in Magic.Command.increasesCounter } &&
                settings.labelPreviousCommands.containsKey(label.name)

        // initialize the dialog with computed values
        val newCommandPopUp = EditLabelDefiningCommand("\\" + commandName, position, labelAnyCommand)
        if (newCommandPopUp.showAndGet()) {
            // save the computed values
            settings.addCommand(LabelingCommandInformation(newCommandPopUp.getCommandName(),
                    newCommandPopUp.getCommandPosition(), newCommandPopUp.getLabelAnyPrevCommand()))
        }
    }

}
