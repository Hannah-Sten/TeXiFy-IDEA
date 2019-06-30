package nl.hannahsten.texifyidea.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import nl.hannahsten.texifyidea.BibtexLanguage
import nl.hannahsten.texifyidea.psi.BibtexTypes

/**
 * @author Hannah Schellekens
 */
open class BibtexFormattingModelBuilder : FormattingModelBuilder {

    private fun createSpacingBuilder(settings: CodeStyleSettings): SpacingBuilder {
        val spacing = SpacingBuilder(settings, BibtexLanguage)
        spacing.around(BibtexTypes.ASSIGNMENT).spaces(1)
        spacing.before(BibtexTypes.SEPARATOR).spaces(0)
        spacing.between(BibtexTypes.TYPE, BibtexTypes.OPEN_BRACE).spaces(0)
        spacing.between(BibtexTypes.TYPE, BibtexTypes.OPEN_PARENTHESIS).spaces(0)
        spacing.between(BibtexTypes.OPEN_BRACE, BibtexTypes.ID).spaces(0)
        spacing.after(BibtexTypes.OPEN_PARENTHESIS).spaces(1)
        spacing.around(BibtexTypes.CONCATENATE).spaces(1)
        spacing.between(BibtexTypes.NORMAL_TEXT_WORD, BibtexTypes.NORMAL_TEXT_WORD).spaces(1)
        spacing.between(BibtexTypes.ENTRY_CONTENT, BibtexTypes.ENDTRY).spaces(1)
        return spacing
    }

    override fun createModel(element: PsiElement, settings: CodeStyleSettings) = FormattingModelProvider.createFormattingModelForPsiFile(
            element.containingFile,
            BibtexBlock(
                    element.node,
                    Wrap.createWrap(WrapType.NONE, false),
                    Alignment.createAlignment(),
                    createSpacingBuilder(settings)
            ),
            settings
    )!!

    override fun getRangeAffectingIndent(file: PsiFile?, offset: Int, elementAtOffset: ASTNode?): TextRange? = null
}