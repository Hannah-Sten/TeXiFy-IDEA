package nl.hannahsten.texifyidea.settings.codestyle

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import nl.hannahsten.texifyidea.grammar.BibtexLanguage

class BibtexCodeStyleSettings(container: CodeStyleSettings) : CustomCodeStyleSettings(BibtexLanguage.id, container)