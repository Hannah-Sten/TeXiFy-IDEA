package nl.hannahsten.texifyidea.intentions

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.refactoring.suggested.startOffset
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.endOffset
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.formatAsLabel

/**
 * @author Hannah Schellekens
 */
open class LatexAddLabelToCommandIntention(val command: SmartPsiElementPointer<LatexCommands>? = null) :
    LatexAddLabelIntention() {

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (file?.isLatexFile() == false) {
            return false
        }

        return findTarget<LatexCommands>(editor, file)?.name in Magic.Command.labeledPrefixes
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) {
            return
        }

        // When no this.command is provided, use the command at the caret as the best guess.
        val command: LatexCommands = this.command?.element
            ?: findTarget(editor, file)
            ?: return

        val prefix = Magic.Command.labeledPrefixes[command.name!!] ?: return

        val factory = LatexPsiHelper(project)

        // For sections we can infer a reasonable label name from the required parameter
        if (Magic.Command.sectionMarkers.contains(command.name)) {
            val required = command.requiredParameters

            // Section commands should all have a required parameter
            val labelString: String = required.getOrNull(0) ?: return
            val createdLabel = getUniqueLabelName(
                labelString.formatAsLabel(),
                prefix, command.containingFile
            )

            // Insert label
            // command -> NoMathContent -> Content -> Container containing the command
            val commandContent = command.parent.parent
            val labelCommand =
                commandContent.parent.addAfter(factory.createLabelCommand(createdLabel.labelText), commandContent)

            // Adjust caret offset.
            val caret = editor.caretModel
            caret.moveToOffset(labelCommand.endOffset())
        }
        else {
            if (Magic.Command.labelAsParameter.contains(command.name)) {
                // Create a label parameter and initiate the rename process
                val createdLabel = getUniqueLabelName(
                    command.name!!.replace("\\", ""),
                    prefix, command.containingFile
                )
                val endMarker =
                    editor.document.createRangeMarker(command.startOffset, command.endOffset())
                createLabelAndStartRename(editor, project, command, createdLabel, endMarker)
            }
            else {
                // Insert and start the \label live template
                editor.caretModel.moveToOffset(command.endOffset())
                val template = TemplateImpl("", "\\label{$prefix:\$__Variable0\$}", "")
                template.addVariable(TextExpression(""), true)
                TemplateManager.getInstance(editor.project).startTemplate(editor, template)
            }
        }
    }
}