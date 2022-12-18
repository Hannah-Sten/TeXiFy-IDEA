package nl.hannahsten.texifyidea.grammar

import com.intellij.lexer.FlexAdapter

/**
 * @author Hannah Schellekens
 */
open class BibtexLexerAdapter : FlexAdapter(BibtexLexer(null))