package nl.hannahsten.texifyidea.index

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IndexSink
import nl.hannahsten.texifyidea.highlighting.LatexAnnotator

fun <T : PsiElement> indexSinkOccurrence(indexSink: IndexSink, index: IndexUtilBase<T>, token: String) {
    // Clear cache to be sure that any update will be reflected (we don't know whether something will be added to the index or whether it's already in there)
    index.cache.clear()
    indexSink.occurrence(index.key(), token)
    LatexAnnotator.allUserDefinedCommands = emptyList()
}