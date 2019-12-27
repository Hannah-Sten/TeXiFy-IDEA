package nl.hannahsten.texifyidea.settings.codestyle

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import nl.hannahsten.texifyidea.LatexLanguage

/**
 *
 * @author Sten Wessel
 */
class LatexCodeStyleSettings(container: CodeStyleSettings) : CustomCodeStyleSettings(LatexLanguage.INSTANCE.id, container) {
    @JvmField var BLANK_LINES_BEFORE_SECTION: Int = 2
}