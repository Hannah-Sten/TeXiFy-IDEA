package nl.hannahsten.texifyidea.templates.postfix

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class LatexPostFixTemplateProvider : PostfixTemplateProvider {
    private val templates = mutableSetOf<PostfixTemplate>(
            LatexSurroundWithGroupPostfixTemplate
    )

    override fun getPresentableName(): String? = "LaTeX"

    override fun getTemplates(): MutableSet<PostfixTemplate> = templates

    override fun isTerminalSymbol(currentChar: Char): Boolean =
            (currentChar == '.')

    override fun afterExpand(file: PsiFile, editor: Editor) {}

    override fun preCheck(copyFile: PsiFile, realEditor: Editor, currentOffset: Int): PsiFile =
            copyFile

    override fun preExpand(file: PsiFile, editor: Editor) {}
}