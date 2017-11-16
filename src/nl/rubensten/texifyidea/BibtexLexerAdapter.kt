package nl.rubensten.texifyidea

import com.intellij.lexer.FlexAdapter
import nl.rubensten.texifyidea.grammar.BibtexLexer

/**
 * @author Ruben Schellekens
 */
open class BibtexLexerAdapter : FlexAdapter(BibtexLexer(null))