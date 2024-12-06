package nl.hannahsten.texifyidea.index

import com.intellij.psi.search.IndexPattern
import com.intellij.psi.search.IndexPatternProvider
import nl.hannahsten.texifyidea.util.magic.CommandMagic

class LatexTodoIndexPatternProvider : IndexPatternProvider {
    override fun getIndexPatterns(): Array<IndexPattern> {
        return CommandMagic.todoCommands.map { IndexPattern("${it.replace("\\", "\\\\")}\\b", true) }.toTypedArray()
    }
}