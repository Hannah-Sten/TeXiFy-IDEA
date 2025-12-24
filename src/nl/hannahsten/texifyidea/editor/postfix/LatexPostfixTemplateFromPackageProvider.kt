package nl.hannahsten.texifyidea.editor.postfix

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.util.insertUsepackage

abstract class LatexPostfixTemplateFromPackageProvider(private val pack: LatexPackage) : PostfixTemplateProvider {

    abstract override fun getTemplates(): MutableSet<PostfixTemplate>

    override fun isTerminalSymbol(currentChar: Char): Boolean = (currentChar == '.' || currentChar == ',')

    override fun afterExpand(file: PsiFile, editor: Editor) {
        file.insertUsepackage(pack)
    }

    override fun preCheck(copyFile: PsiFile, realEditor: Editor, currentOffset: Int): PsiFile =
        copyFile

    override fun preExpand(file: PsiFile, editor: Editor) {}

    companion object {

        fun getProvider(pack: LatexPackage?): PostfixTemplateProvider = when (pack) {
            LatexPackage.AMSMATH -> LatexPostfixTemplateFromAmsMathProvider()
            LatexPackage.AMSFONTS -> LatexPostfixTemplateFromAmsFontsProvider()
            LatexPackage.BM -> LatexPostfixTemplateFromBmProvider()
            else -> LatexPostFixTemplateProvider()
        }
    }
}

class LatexPostfixTemplateFromAmsMathProvider : LatexPostfixTemplateFromPackageProvider(LatexPackage.AMSMATH) {

    override fun getTemplates(): MutableSet<PostfixTemplate> = mutableSetOf(
        LatexWrapWithTextPostfixTemplate
    )
}

class LatexPostfixTemplateFromAmsFontsProvider : LatexPostfixTemplateFromPackageProvider(LatexPackage.AMSFONTS) {

    override fun getTemplates(): MutableSet<PostfixTemplate> = mutableSetOf(
        LatexWrapWithMathbbPostfixTemplate
    )
}

class LatexPostfixTemplateFromBmProvider : LatexPostfixTemplateFromPackageProvider(LatexPackage.BM) {

    override fun getTemplates(): MutableSet<PostfixTemplate> = mutableSetOf(
        LatexWrapWithBmPostfixTemplate
    )
}