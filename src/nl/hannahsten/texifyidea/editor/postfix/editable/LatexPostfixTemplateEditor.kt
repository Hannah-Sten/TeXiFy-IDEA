package nl.hannahsten.texifyidea.editor.postfix.editable

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.postfix.settings.PostfixTemplateEditorBase
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.psi.JavaCodeFragmentFactory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.util.ui.FormBuilder
import nl.hannahsten.texifyidea.editor.postfix.LatexPostFixTemplateProvider
import javax.swing.JComponent
import javax.swing.JPanel

class LatexPostfixTemplateEditor(
    private val templateProvider: LatexPostFixTemplateProvider,
    private val panel: JPanel = FormBuilder.createFormBuilder()
        .addLabeledComponent("Context", ComboBox(LatexPostfixTemplateExpressionCondition.values().toTypedArray()))
        .panel
)
    : PostfixTemplateEditorBase<LatexPostfixTemplateExpressionCondition>(templateProvider, createEditor(), true) {

    override fun createTemplate(templateId: String, templateName: String): PostfixTemplate {
        return LatexEditablePostfixTemplate(templateId, templateName, myTemplateEditor.document.text, LatexPostfixTemplateDefaultExpressionCondition(), templateProvider)
    }

    override fun getComponent(): JComponent {
        return panel
    }

    override fun fillConditions(group: DefaultActionGroup) {
        group.add(AddConditionAction(LatexPostfixTemplateDefaultExpressionCondition()))
        group.add(AddConditionAction(LatexPostfixTemplateMathOnlyExpressionCondition()))
        group.add(AddConditionAction(LatexPostfixTemplateTextOnlyExpressionCondition()))
    }

    companion object {
        private fun createEditor(): Editor {
            return createEditor(null, createDocument(ProjectManager.getInstance().defaultProject))
        }

        private fun createDocument(project: Project?): Document {
            if (project == null) {
                return EditorFactory.getInstance().createDocument("")
            }
            val factory = JavaCodeFragmentFactory.getInstance(project)
            val fragment = factory.createCodeBlockCodeFragment("", null, true)
            DaemonCodeAnalyzer.getInstance(project).setHighlightingEnabled(fragment, false)
            return PsiDocumentManager.getInstance(project).getDocument(fragment)
                ?: EditorFactory.getInstance().createDocument("")
        }
    }
}