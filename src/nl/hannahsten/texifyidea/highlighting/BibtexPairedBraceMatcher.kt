package nl.hannahsten.texifyidea.highlighting

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import nl.hannahsten.texifyidea.psi.BibtexTypes

/**
 * @author Hannah Schellekens
 */
open class BibtexPairedBraceMatcher : PairedBraceMatcher {

    private val bracePairs = arrayOf(
        BracePair(BibtexTypes.OPEN_BRACE, BibtexTypes.CLOSE_BRACE, false),
        BracePair(BibtexTypes.OPEN_PARENTHESIS, BibtexTypes.CLOSE_PARENTHESIS, false),
        BracePair(BibtexTypes.QUOTES, BibtexTypes.END_QUOTES, false)
    )

    override fun getPairs() = bracePairs

    override fun isPairedBracesAllowedBeforeType(leftBrace: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int) = openingBraceOffset
}