package nl.hannahsten.texifyidea.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

/**
 * @author Hannah Schellekens
 */
open class BibtexFormattingModelBuilder : FormattingModelBuilder {

    override fun createModel(context: FormattingContext) = FormattingModelProvider.createFormattingModelForPsiFile(
        context.containingFile,
        BibtexBlock(
            context.node,
            Wrap.createWrap(WrapType.NONE, false),
            Alignment.createAlignment(),
            createBibtexSpacingBuilder(context.codeStyleSettings)
        ),
        context.codeStyleSettings
    )!!

    override fun getRangeAffectingIndent(file: PsiFile?, offset: Int, elementAtOffset: ASTNode?): TextRange? = null
}