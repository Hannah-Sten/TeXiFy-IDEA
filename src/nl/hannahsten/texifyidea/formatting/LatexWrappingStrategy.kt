package nl.hannahsten.texifyidea.formatting

import com.intellij.application.options.CodeStyle
import com.intellij.formatting.Wrap
import com.intellij.formatting.WrapType
import com.intellij.lang.ASTNode

/**
 *
 * @author Sten Wessel
 */
class LatexWrappingStrategy {

    fun getNormalWrap(node: ASTNode): Wrap? {
        val settings = CodeStyle.getLanguageSettings(node.psi.containingFile)
        return if (settings.WRAP_LONG_LINES) {
            Wrap.createWrap(WrapType.NORMAL, false)
        }
        else getNoneWrap()
    }

    fun getNoneWrap(): Wrap? {
        return Wrap.createWrap(WrapType.NONE, false)
    }
}