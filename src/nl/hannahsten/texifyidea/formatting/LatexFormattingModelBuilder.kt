package nl.hannahsten.texifyidea.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

/**
 * @author Sten Wessel
 */
class LatexFormattingModelBuilder : FormattingModelBuilder {

    override fun createModel(context: FormattingContext): FormattingModel {
        val codeStyleSettings = context.codeStyleSettings
        return FormattingModelProvider.createFormattingModelForPsiFile(
            context.containingFile,
            LatexBlock(
                context.node,
                Wrap.createWrap(WrapType.NONE, false),
                Alignment.createAlignment(),
                createSpacingBuilder(codeStyleSettings),
                codeStyleSettings

            ),
            context.codeStyleSettings
        )
    }

    override fun getRangeAffectingIndent(file: PsiFile, offset: Int, elementAtOffset: ASTNode): TextRange? = null
}