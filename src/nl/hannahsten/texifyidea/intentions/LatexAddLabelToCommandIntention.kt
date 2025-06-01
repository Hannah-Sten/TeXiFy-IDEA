package nl.hannahsten.texifyidea.intentions

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNoMathContent
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.settings.conventions.LabelConventionType
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsSettingsManager
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.formatAsLabel
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.endOffset
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

/**
 * @author Hannah Schellekens
 */
open class LatexAddLabelToCommandIntention(val command: SmartPsiElementPointer<LatexCommands>? = null) :
    LatexAddLabelIntention("Add label to command") {

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (file?.isLatexFile() == false) {
            return false
        }

        val targetName = findTarget<LatexCommands>(editor, file)?.name
        val conventionSettings = TexifyConventionsSettingsManager.getInstance(project).getSettings()

        return conventionSettings.getLabelConvention(targetName, LabelConventionType.COMMAND)?.enabled ?: false
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) {
            return
        }

        // When no this.command is provided, use the command at the caret as the best guess.
        val command: LatexCommands = this.command?.element
            ?: findTarget(editor, file)
            ?: return

        val conventionSettings = TexifyConventionsSettingsManager.getInstance(project).getSettings()
        val prefix = conventionSettings.getLabelConvention(command.name, LabelConventionType.COMMAND)?.prefix ?: return

        val factory = LatexPsiHelper(project)

        // For sections we can infer a reasonable label name from the required parameter
        if (CommandMagic.sectionNameToLevel.contains(command.name)) {
            val required = command.getRequiredParameters()
            // Section commands should all have a required parameter
            val labelString: String = required.getOrNull(0) ?: return
            val createdLabel = getUniqueLabelName(
                labelString.formatAsLabel(),
                prefix, command.containingFile
            )

            // Insert label
            val commandContent = command.firstParentOfType(LatexNoMathContent::class) ?: return
            val labelCommand =
                commandContent.parent?.addAfter(factory.createLabelCommand(createdLabel.labelText), commandContent) ?: return

            // Adjust caret offset.
            val caret = editor.caretModel
            caret.moveToOffset(labelCommand.endOffset())
        }
        else {
            if (CommandMagic.labelAsParameter.contains(command.name)) {
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