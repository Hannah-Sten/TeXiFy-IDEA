package nl.hannahsten.texifyidea.formatting

import com.intellij.formatting.Wrap
import com.intellij.formatting.WrapType
import com.intellij.lang.ASTNode
import com.intellij.psi.codeStyle.CodeStyleSettings
import nl.hannahsten.texifyidea.grammar.LatexLanguage

/**
 *
 * @author Sten Wessel
 */
object LatexWrappingStrategy {

    fun getNormalWrap(settings: CodeStyleSettings, node: ASTNode): Wrap? {
        val latexSettings = settings.getCommonSettings(LatexLanguage)
        return if (latexSettings.WRAP_LONG_LINES) {
            Wrap.createWrap(WrapType.NORMAL, false)
        }
        else getNoneWrap()
    }

    fun getNoneWrap(): Wrap? {
        return Wrap.createWrap(WrapType.NONE, false)
    }
}