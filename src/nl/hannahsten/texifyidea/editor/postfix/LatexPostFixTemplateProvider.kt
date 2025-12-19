package nl.hannahsten.texifyidea.editor.postfix

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplatesUtils
import com.intellij.codeInsight.template.postfix.templates.editable.PostfixTemplateEditor
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.editor.postfix.editable.LatexEditablePostfixTemplate
import nl.hannahsten.texifyidea.editor.postfix.editable.LatexPostfixTemplateEditor
import nl.hannahsten.texifyidea.editor.postfix.editable.LatexPostfixTemplateExpressionCondition
import org.jdom.Element

class LatexPostFixTemplateProvider : PostfixTemplateProvider, CompletionContributor() {

    private val templates = mutableSetOf<PostfixTemplate>(
        LatexWrapWithGroupPostfixTemplate,
        LatexWrapWithOpenGroupPostfixTemplate,
        LatexWrapWithInlineMathPostfixTemplate
    )

    private val wrapWithTextCommandTemplates = mutableSetOf<PostfixTemplate>(
        LatexWrapWithBoldFacePostfixTemplate,
        LatexWrapWithItalicFacePostfixTemplate,
        LatexWrapWithEmphPostfixTemplate,
        LatexWrapWithTypewriterPostfixTemplate
    )

    private val wrapWithMathCommandTemplates = mutableSetOf<PostfixTemplate>(
        LatexWrapWithTildePostfixTemplate,
        LatexWrapWithHatPostfixTemplate,
        LatexWrapWithBarPostfixTemplate,
        LatexWrapWithSquareRootPostfixTemplate,
        LatexWrapWithOverlinePostfixTemplate,
        LatexWrapWithUnderlinePostfixTemplate,
        LatexWrapWithMathcalPostfixTemplate
    )

    override fun getTemplates(): MutableSet<PostfixTemplate> = (templates + wrapWithTextCommandTemplates + wrapWithMathCommandTemplates) as MutableSet<PostfixTemplate>

    /**
     * To decide whether this template provider can add new templates, idea checks it its presentable name is non-null.
     */
    override fun getPresentableName() = "LaTeX"

    override fun createEditor(templateToEdit: PostfixTemplate?): PostfixTemplateEditor? {
        return when (templateToEdit) {
            null -> {
                LatexPostfixTemplateEditor(this)
            }
            is LatexEditablePostfixTemplate if !templateToEdit.isBuiltin -> {
                LatexPostfixTemplateEditor(this).apply { setTemplate(templateToEdit) }
            }

            else -> null
        }
    }

    override fun isTerminalSymbol(currentChar: Char): Boolean = (currentChar == '.')

    override fun afterExpand(file: PsiFile, editor: Editor) {}

    override fun preCheck(copyFile: PsiFile, realEditor: Editor, currentOffset: Int): PsiFile =
        copyFile

    override fun preExpand(file: PsiFile, editor: Editor) {}

    override fun readExternalTemplate(id: String, name: String, template: Element): PostfixTemplate? {
        val liveTemplate = PostfixTemplatesUtils.readExternalLiveTemplate(template, this) ?: return null
        val conditions = PostfixTemplatesUtils.readExternalConditions(template) { condition: Element? -> LatexPostfixTemplateExpressionCondition.readExternal(condition!!) }

        return LatexEditablePostfixTemplate(id, name, liveTemplate, conditions, this)
    }

    override fun writeExternalTemplate(template: PostfixTemplate, parentElement: Element) {
        if (template is LatexEditablePostfixTemplate) {
            PostfixTemplatesUtils.writeExternalTemplate(template, parentElement)
        }
    }
}