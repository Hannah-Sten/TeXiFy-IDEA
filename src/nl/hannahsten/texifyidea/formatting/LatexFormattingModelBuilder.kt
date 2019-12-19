package nl.hannahsten.texifyidea.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.psi.LatexTypes

/**
 * @author Sten Wessel
 */
class LatexFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(element: PsiElement, settings: CodeStyleSettings): FormattingModel {
        return FormattingModelProvider.createFormattingModelForPsiFile(
                element.containingFile,
                LatexBlock(
                        element.node,
                        Wrap.createWrap(WrapType.NONE, false),
                        Alignment.createAlignment(),
                        createSpaceBuilder(settings)
                ),
                settings
        )
    }

    override fun getRangeAffectingIndent(file: PsiFile, offset: Int, elementAtOffset: ASTNode): TextRange? {
        return null
    }

    companion object {
        private fun createSpaceBuilder(settings: CodeStyleSettings): SpacingBuilder {
            val spacingBuilder = SpacingBuilder(settings, LatexLanguage.INSTANCE)
            spacingBuilder.between(LatexTypes.NORMAL_TEXT_WORD, LatexTypes.NORMAL_TEXT_WORD)
                    .spaces(1)
            spacingBuilder.around(LatexTypes.ENVIRONMENT_CONTENT)
                    .lineBreakInCode()
            return spacingBuilder
        }
    }
}