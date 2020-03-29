package nl.hannahsten.texifyidea.templates.postfix

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

object LatexPostFixTemplateProvider : PostfixTemplateProvider {
    private val templates = mutableSetOf<PostfixTemplate>(
            LatexWrapWithGroupPostfixTemplate,
            LatexWrapWithOpenGroupPostfixTemplate,
            LatexWrapWithInlineMathPostfixTemplate
    )

    private val wrapWithTextCommandTemplates = mutableSetOf<PostfixTemplate>(
            LatexWrapWithBoldFacePostfixTemplate,
            LatexWrapWithItalicFacePostfixTemplate,
            LatexWrapWithEmphPostfixTemplate
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

    override fun getPresentableName(): String? = "LaTeX"

    override fun getTemplates(): MutableSet<PostfixTemplate> = (templates + wrapWithTextCommandTemplates + wrapWithMathCommandTemplates) as MutableSet<PostfixTemplate>

    override fun isTerminalSymbol(currentChar: Char): Boolean =
            (currentChar == '.')

    override fun afterExpand(file: PsiFile, editor: Editor) {}

    override fun preCheck(copyFile: PsiFile, realEditor: Editor, currentOffset: Int): PsiFile =
            copyFile

    override fun preExpand(file: PsiFile, editor: Editor) {}
}