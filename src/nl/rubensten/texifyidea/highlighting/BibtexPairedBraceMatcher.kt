package nl.rubensten.texifyidea.highlighting

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import nl.rubensten.texifyidea.psi.BibtexTypes

/**
 * @author Ruben Schellekens
 */
open class BibtexPairedBraceMatcher : PairedBraceMatcher {

    companion object {

        val PAIRS = arrayOf(
                BracePair(BibtexTypes.OPEN_BRACE, BibtexTypes.CLOSE_BRACE, false),
                BracePair(BibtexTypes.OPEN_PARENTHESIS, BibtexTypes.CLOSE_PARENTHESIS, false),
                BracePair(BibtexTypes.QUOTES, BibtexTypes.END_QUOTES, false)
        )
    }

    override fun getPairs() = PAIRS

    override fun isPairedBracesAllowedBeforeType(leftBrace: IElementType, contextType: IElementType?): Boolean {
        return true
    }

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int) = openingBraceOffset
}