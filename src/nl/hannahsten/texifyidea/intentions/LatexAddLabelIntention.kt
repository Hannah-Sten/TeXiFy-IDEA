package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.isLatexFile
import kotlin.math.max

/**
 * @author Hannah Schellekens
 */
open class LatexAddLabelIntention(val command: SmartPsiElementPointer<LatexCommands>? = null) : TexifyIntentionBase("Add label") {

    private fun findCommand(editor: Editor?, file: PsiFile?): LatexCommands? {
        val offset = editor?.caretModel?.offset ?: return null
        val element = file?.findElementAt(offset) ?: return null
        // Also check one position back, because we want it to trigger in \section{a}<caret>
        return element as? LatexCommands ?: element.parentOfType(LatexCommands::class)
            ?: file.findElementAt(max(0, offset - 1)) as? LatexCommands
            ?: file.findElementAt(max(0, offset - 1))?.parentOfType(LatexCommands::class)
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (file?.isLatexFile() == false) {
            return false
        }

        return findCommand(editor, file)?.name in Magic.Command.labeledPrefixes
    }

    override fun startInWriteAction() = true

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) {
            return
        }

        // When no this.command is provided, use the command at the caret as the best guess.
        val command: LatexCommands = this.command?.element
                ?: findCommand(editor, file)
                ?: return

        // Determine label name.
        val labelString: String = if (Magic.Command.labelAsParameter.contains(command.name)) {
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
                command.name ?: return
            }
        }

        val createdLabel = getUniqueLabelName(
            labelString.formatAsLabel(),
            Magic.Command.labeledPrefixes[command.name!!], command.containingFile
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

    private fun getUniqueLabelName(base: String, prefix: String?, file: PsiFile): String {
        val labelBase = "$prefix:$base"
        val allLabels = file.findLatexAndBibtexLabelStringsInFileSet()
        return appendCounter(labelBase, allLabels)
    }

    /**
     * Keeps adding a counter behind the label until there is no other label with that name.
     */
    private fun appendCounter(label: String, allLabels: Set<String>): String {
        var counter = 2
        var candidate = label

        while (allLabels.contains(candidate)) {
            candidate = label + (counter++)
        }

        return candidate
    }
}