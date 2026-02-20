package nl.hannahsten.texifyidea.editor.postfix

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.util.insertUsepackage

abstract class LatexPostfixTemplateFromPackageProvider(private val pack: LatexLib) : PostfixTemplateProvider {

    abstract override fun getTemplates(): MutableSet<PostfixTemplate>

    override fun isTerminalSymbol(currentChar: Char): Boolean = (currentChar == '.' || currentChar == ',')

    override fun afterExpand(file: PsiFile, editor: Editor) {
        file.insertUsepackage(pack)
    }

    override fun preCheck(copyFile: PsiFile, realEditor: Editor, currentOffset: Int): PsiFile =
        copyFile

    override fun preExpand(file: PsiFile, editor: Editor) {}

    companion object {

        fun getProvider(pack: LatexLib?): PostfixTemplateProvider = when (pack) {
            LatexLib.AMSMATH -> LatexPostfixTemplateFromAmsMathProvider()
            LatexLib.AMSFONTS -> LatexPostfixTemplateFromAmsFontsProvider()
            LatexLib.BM -> LatexPostfixTemplateFromBmProvider()
            else -> LatexPostFixTemplateProvider()
        }
    }
}

class LatexPostfixTemplateFromAmsMathProvider : LatexPostfixTemplateFromPackageProvider(LatexLib.AMSMATH) {

    override fun getTemplates(): MutableSet<PostfixTemplate> = mutableSetOf(
        LatexWrapWithTextPostfixTemplate
    )
}

class LatexPostfixTemplateFromAmsFontsProvider : LatexPostfixTemplateFromPackageProvider(LatexLib.AMSFONTS) {

    override fun getTemplates(): MutableSet<PostfixTemplate> = mutableSetOf(
        LatexWrapWithMathbbPostfixTemplate
    )
}

class LatexPostfixTemplateFromBmProvider : LatexPostfixTemplateFromPackageProvider(LatexLib.BM) {

    override fun getTemplates(): MutableSet<PostfixTemplate> = mutableSetOf(
        LatexWrapWithBmPostfixTemplate
    )
}