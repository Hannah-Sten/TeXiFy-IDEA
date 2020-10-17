package nl.hannahsten.texifyidea.editor.postfix

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.lang.Package
import nl.hannahsten.texifyidea.util.insertUsepackage

abstract class LatexPostfixTemplateFromPackageProvider(private val pack: Package) : PostfixTemplateProvider {
    abstract override fun getTemplates(): MutableSet<PostfixTemplate>

    override fun isTerminalSymbol(currentChar: Char): Boolean = (currentChar == '.' || currentChar == ',')

    override fun afterExpand(file: PsiFile, editor: Editor) {
        file.insertUsepackage(pack)
    }

    override fun preCheck(copyFile: PsiFile, realEditor: Editor, currentOffset: Int): PsiFile =
        copyFile

    override fun preExpand(file: PsiFile, editor: Editor) {}

    companion object {
        fun getProvider(pack: Package?): PostfixTemplateProvider {
            return when (pack) {
                Package.AMSMATH -> LatexPostfixTemplateFromAmsMathProvider
                Package.AMSFONTS -> LatexPostfixTemplateFromAmsFontsProvider
                Package.BM -> LatexPostfixTemplateFromBmProvider
                else -> LatexPostFixTemplateProvider
            }
        }
    }
}

object LatexPostfixTemplateFromAmsMathProvider : LatexPostfixTemplateFromPackageProvider(Package.AMSMATH) {
    override fun getTemplates(): MutableSet<PostfixTemplate> = mutableSetOf(
        LatexWrapWithTextPostfixTemplate
    )
}

object LatexPostfixTemplateFromAmsFontsProvider : LatexPostfixTemplateFromPackageProvider(Package.AMSFONTS) {
    override fun getTemplates(): MutableSet<PostfixTemplate> = mutableSetOf(
        LatexWrapWithMathbbPostfixTemplate
    )
}

object LatexPostfixTemplateFromBmProvider : LatexPostfixTemplateFromPackageProvider(Package.BM) {
    override fun getTemplates(): MutableSet<PostfixTemplate> = mutableSetOf(
        LatexWrapWithBmPostfixTemplate
    )
}