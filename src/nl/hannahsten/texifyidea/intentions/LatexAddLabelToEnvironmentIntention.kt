package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.settings.conventions.LabelConventionType
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsSettingsManager
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.formatAsLabel
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.parser.collectSubtreeTyped
import nl.hannahsten.texifyidea.util.parser.endOffset

open class LatexAddLabelToEnvironmentIntention(val environment: SmartPsiElementPointer<LatexEnvironment>? = null) :
    LatexAddLabelIntention("Add label to environment") {

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (file?.isLatexFile() == false) {
            return false
        }

        val targetName = findTarget<LatexEnvironment>(editor, file)?.getEnvironmentName()
        val conventionSettings = TexifyConventionsSettingsManager.getInstance(project).getSettings()
        return conventionSettings.getLabelConvention(targetName, LabelConventionType.ENVIRONMENT)?.enabled
            ?: false
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return

        val environment = this.environment?.element
            ?: findTarget(editor, file)
            ?: return

        val helper = LatexPsiHelper(project)

        // Determine label name.
        val conventionSettings = TexifyConventionsSettingsManager.getInstance(project).getSettings()
        val prefix =
            conventionSettings.getLabelConvention(environment.getEnvironmentName(), LabelConventionType.ENVIRONMENT)?.prefix
                ?: return

        val createdLabel = getUniqueLabelWithPrefix(
            environment.getEnvironmentName().formatAsLabel(),
            prefix, environment.containingFile
        )

        if (EnvironmentMagic.labelAsParameter.contains(environment.getEnvironmentName())) {
            val endMarker = editor.document.createRangeMarker(environment.startOffset, environment.endOffset())
            createLabelAndStartRename(editor, project, environment.beginCommand, createdLabel, endMarker)
        }
        else {
            // in a float environment the label must be inserted after a caption
            val labelCommand = helper.addToContent(
                environment, helper.createLabelCommand(createdLabel.labelText),
                environment.environmentContent?.collectSubtreeTyped<LatexCommands>()
                    ?.findLast { c -> c.name == "\\caption" }
            )

            // Adjust caret offset
            val caretModel = editor.caretModel
            caretModel.moveToOffset(labelCommand.endOffset())
        }
    }
}