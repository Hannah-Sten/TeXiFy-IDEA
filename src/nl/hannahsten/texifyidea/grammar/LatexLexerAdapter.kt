package nl.hannahsten.texifyidea.grammar

import com.intellij.lexer.FlexAdapter

/**
 * @author Sten Wessel
 */
class LatexLexerAdapter : FlexAdapter(LatexLexer(null))