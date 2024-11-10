package nl.hannahsten.texifyidea.index

import com.intellij.lexer.Lexer
import com.intellij.psi.impl.cache.impl.BaseFilterLexer
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer
import com.intellij.psi.impl.cache.impl.todo.LexerBasedTodoIndexer
import nl.hannahsten.texifyidea.grammar.LatexLexerAdapter
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * Counts the "to do" item, so that it shows up in the Project "to do" window and the count of the number of items is correct.
 */
class LatexTodoIndexer : LexerBasedTodoIndexer() {
    override fun createLexer(consumer: OccurrenceConsumer): Lexer {
        return object : BaseFilterLexer(LatexLexerAdapter(), consumer) {
            override fun advance() {
                val tokenType = delegate.tokenType
                if (tokenType == LatexTypes.COMMAND_TOKEN && delegate.tokenText in CommandMagic.todoCommands) {
                    advanceTodoItemCountsInToken()
                }
                delegate.advance()
            }
        }
    }
}