package nl.hannahsten.texifyidea.grammar

import com.intellij.lexer.FlexAdapter
import nl.hannahsten.texifyidea.grammar.BibtexLexer

/**
 * @author Hannah Schellekens
 */
open class BibtexLexerAdapter : FlexAdapter(BibtexLexer(null))