package nl.hannahsten.texifyidea.intentions

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
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

    override fun startInWriteAction() = true

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) {
            return
        }

        // When no this.command is provided, use the command at the caret as the best guess.
        val command: LatexCommands = this.command?.element
            ?: findTarget(editor, file)
            ?: return

        // Determine label name.
        val labelString: String? = if (Magic.Command.labelAsParameter.contains(command.name)) {
            // For parameter labeled commands we use the command name itself
            command.name!!
        }
        else {
            // For all other commands we use the first required parameter
            val required = command.requiredParameters
            if (required.isNotEmpty()) {
                required[0]
            }
            else {
                null
            }
        }

        val prefix = Magic.Command.labeledPrefixes[command.name!!]

        if (labelString != null) {
            val createdLabel = getUniqueLabelName(
                labelString.formatAsLabel(),
                prefix, command.containingFile
            )

            val factory = LatexPsiHelper(project)

            val labelCommand = if (Magic.Command.labelAsParameter.contains(command.name)) {
                factory.setOptionalParameter(command, "label", "{$createdLabel}")
            }
            else {
                // Insert label
                // command -> NoMathContent -> Content -> Container containing the command
                val commandContent = command.parent.parent
                commandContent.parent.addAfter(factory.createLabelCommand(createdLabel), commandContent)
            }
            // Adjust caret offset.
            val caret = editor.caretModel
            caret.moveToOffset(labelCommand.endOffset())
        }
        else {
            editor.caretModel.moveToOffset(command.endOffset())
            val template = TemplateImpl("", "\\label{$prefix:\$__Variable0\$}", "")
            template.addVariable(TextExpression(""), true)
            TemplateManager.getInstance(editor.project).startTemplate(editor, template)
        }
    }
}