package nl.hannahsten.texifyidea.editor.postfix.editable

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.template.postfix.settings.PostfixTemplateEditorBase
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate
import com.intellij.grazie.utils.toSet
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import nl.hannahsten.texifyidea.editor.postfix.LatexPostFixTemplateProvider
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.isLatexProject
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class LatexPostfixTemplateEditor(private val templateProvider: LatexPostFixTemplateProvider) :
    PostfixTemplateEditorBase<LatexPostfixTemplateExpressionCondition>(templateProvider, createEditor(), true) {

    private val panel: JPanel = FormBuilder.createFormBuilder()
        .addComponentFillVertically(myEditTemplateAndConditionsPanel, UIUtil.DEFAULT_VGAP)
        .addComponent(JLabel("Use the Custom Postfix Templates plugin to create more complex postfix templates.").apply { icon = AllIcons.General.Information })
        .panel

    override fun createTemplate(templateId: String, templateName: String): PostfixTemplate {
        return LatexEditablePostfixTemplate(templateId, templateName, myTemplateEditor.document.text, myExpressionTypesListModel.elements().toSet(), templateProvider)
    }

    override fun getComponent(): JComponent {
        return panel
    }

    override fun fillConditions(group: DefaultActionGroup) {
        group.add(AddConditionAction(LatexPostfixTemplateMathOnlyExpressionCondition()))
        group.add(AddConditionAction(LatexPostfixTemplateTextOnlyExpressionCondition()))
    }

    companion object {
        private fun createEditor(): Editor {
            val project = ProjectManager.getInstance().openProjects.firstOrNull { it.isLatexProject() } ?: ProjectManager.getInstance().defaultProject
            return EditorFactory.getInstance().createEditor(createDocument(project), project, LatexFileType, false)
        }

        private fun createDocument(project: Project?): Document {
            if (project == null) {
                return EditorFactory.getInstance().createDocument("")
            }
            val fragment = LatexPsiHelper(project).createFromText("")
            DaemonCodeAnalyzer.getInstance(project).setHighlightingEnabled(fragment, false)
            return PsiDocumentManager.getInstance(project).getDocument(fragment)
                ?: EditorFactory.getInstance().createDocument("")
        }
    }
}