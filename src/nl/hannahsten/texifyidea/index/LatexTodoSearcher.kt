package nl.hannahsten.texifyidea.index

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.search.IndexPattern
import com.intellij.psi.search.IndexPatternOccurrence
import com.intellij.psi.search.searches.IndexPatternSearch
import com.intellij.util.Processor
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.matches

/**
 * Provides the "to do" item in the toolwindow.
 */
@Suppress("UnstableApiUsage")
class LatexTodoSearcher : QueryExecutorBase<IndexPatternOccurrence, IndexPatternSearch.SearchParameters>() {
    override fun processQuery(queryParameters: IndexPatternSearch.SearchParameters, consumer: Processor<in IndexPatternOccurrence>) {
        val file = queryParameters.file as? LatexFile ?: return

        queryParameters.patternProvider.indexPatterns.forEach { pattern ->
            file.commandsInFile().filter { it.name in CommandMagic.todoCommands }.filter { pattern.pattern?.matches(it.name) == true }
                .forEach {
                    consumer.process(LatexTodoOccurrence(file, it.textRange, pattern))
                }
        }
    }
}

private data class LatexTodoOccurrence(private val file: LatexFile, private val textRange: TextRange, private val pattern: IndexPattern) : IndexPatternOccurrence {
    override fun getFile(): PsiFile = file
    override fun getTextRange(): TextRange = textRange
    override fun getPattern(): IndexPattern = pattern
}