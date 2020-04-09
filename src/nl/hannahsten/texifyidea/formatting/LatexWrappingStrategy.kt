package nl.hannahsten.texifyidea.formatting

import com.intellij.formatting.Wrap
import com.intellij.formatting.WrapType

/**
 *
 * @author Sten Wessel
 */
class LatexWrappingStrategy {

    fun getWrap(): Wrap? {
        return Wrap.createWrap(WrapType.NONE, false)
    }
}