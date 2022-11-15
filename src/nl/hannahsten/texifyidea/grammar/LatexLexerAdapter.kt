package nl.hannahsten.texifyidea.grammar

import com.intellij.lexer.FlexAdapter
import nl.hannahsten.texifyidea.grammar.LatexLexer

/**
 * @author Sten Wessel
 */
class LatexLexerAdapter : FlexAdapter(LatexLexer(null))